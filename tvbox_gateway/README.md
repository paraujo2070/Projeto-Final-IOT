# TV Box Gateway

Implementacao do gateway da TV box para o projeto de IoT.

Este componente e responsavel por receber os dados gerados pelos dispositivos de borda, armazena-los localmente e encaminha-los para a nuvem via MQTT.

## Objetivo

A TV box funciona como intermediaria entre a borda e a nuvem.

O fluxo geral e:

1. o app Android executado no dispositivo de borda coleta os dados
2. o app publica esses dados via MQTT para a TV box
3. a TV box recebe as mensagens em um broker MQTT local
4. o gateway salva as mensagens em uma outbox SQLite
5. o gateway transforma o payload quando necessario
6. o gateway reenvía os dados para a nuvem

Esse desenho permite desacoplar a coleta local da conectividade externa. Se a nuvem ficar indisponivel, a TV box continua recebendo os dados e tenta reenviá-los depois.

## Papel da TV Box na Arquitetura

A TV box concentra duas responsabilidades:

- atuar como broker MQTT local para os dispositivos de borda
- atuar como servico de encaminhamento para a nuvem

Na pratica, isso faz da TV box um gateway local.

## Estrutura da Pasta

```text
tvbox_gateway/
├── gateway/
├── mosquitto/
├── systemd/
├── gateway.env.example
├── requirements.txt
└── README.md
```

Arquivos e diretorios de runtime, como banco local, logs auxiliares e configuracoes sensiveis, nao fazem parte da versao do codigo e ficam fora do Git.

### `gateway/`

Contem a implementacao principal do servico Python.

Arquivos:

- `main.py`
  Orquestra o servico. Carrega a configuracao, inicializa os componentes e executa o ciclo principal do gateway.

- `config.py`
  Define a estrutura de configuracao do servico e faz a leitura das variaveis de ambiente.

- `local_subscriber.py`
  Responsavel por ouvir o broker local e registrar cada mensagem recebida na outbox do gateway.

- `cloud_forwarder.py`
  Responsavel por ler as mensagens pendentes, transformar o payload quando necessario e reenviá-las para a nuvem.

- `storage.py`
  Implementa a outbox SQLite usada pelo gateway.

- `models.py`
  Define estruturas auxiliares internas do servico.

### `mosquitto/`

Contem a configuracao do broker MQTT local executado na TV box.

Esse broker recebe as mensagens vindas dos dispositivos de borda.

### `systemd/`

Contem a unidade `tvbox-gateway.service`, usada para executar o gateway como servico do sistema.

Isso permite:

- iniciar automaticamente no boot
- manter o processo em execucao
- reiniciar automaticamente em caso de falha

## Funcionamento do Gateway

### 1. Recepcao local

O gateway escuta o broker MQTT local da TV box.

Na implementacao atual, ele assina o topico:

`telemetry`

Esse valor foi mantido para compatibilidade com o app de borda existente.

Quando uma mensagem chega:

- o topico e o payload sao capturados
- o payload e validado como JSON
- o destino cloud e calculado
- a mensagem e armazenada na outbox local

### 2. Outbox local

Antes de qualquer envio para a nuvem, cada mensagem recebida e salva em um banco SQLite.

Essa outbox guarda:

- topico de origem
- topico de destino na nuvem
- payload original
- QoS
- horario de recebimento
- contador de tentativas
- ultima mensagem de erro

Essa camada existe para evitar perda de mensagens.

### 3. Encaminhamento para a nuvem

O servico executa ciclos periodicos de flush da outbox.

Em cada ciclo:

- ele le mensagens pendentes
- transforma o payload se necessario
- publica no broker cloud
- remove da outbox se o envio foi bem-sucedido

Se houver falha no envio:

- a mensagem permanece na outbox
- o erro e registrado
- a tentativa sera refeita em um ciclo posterior

## Integracao MQTT Local

O broker MQTT local e o ponto de entrada dos dados da borda.

Na configuracao atual:

- host local do broker: `127.0.0.1`
- porta local: `1883`
- topico de assinatura do gateway: `telemetry`

Os dispositivos de borda publicam na TV box, e o gateway consome essas mensagens internamente.

## Integracao com a Nuvem

O gateway suporta envio MQTT para a nuvem.

Na configuracao atual do projeto, a nuvem utilizada e a Ubidots.

Parametros usados:

- host: `industrial.api.ubidots.com`
- porta: `1883`
- autenticacao: token no `username`
- topico: `/v1.6/devices/teste_projeto_final`

## Formato do Payload na Nuvem

O payload vindo do app de borda nao e reenviado de forma bruta.

O gateway converte o JSON recebido para um formato mais apropriado ao consumo na nuvem.

As variaveis atualmente enviadas sao:

- `temperatura_c`
- `umidade_relativa_pct`
- `barometro_hpa_media`
- `barometro_hpa_variancia`
- `accel_x_variancia`
- `accel_y_variancia`
- `accel_z_variancia`
- `gyro_x_variancia`
- `gyro_y_variancia`
- `gyro_z_variancia`

Cada variavel enviada para a nuvem contem:

- `value`
- `timestamp`
- `context`

O `context` inclui:

- `device_id`
- `gateway_id`
- `source_timestamp`
- `status_sistema`
- `label_coleta`
- `janela_amostragem_segundos`

Isso permite que a nuvem identifique a origem dos dados mesmo quando varios dispositivos publicam para o mesmo gateway.

## Arquivo de Configuracao

O arquivo versionado de referencia e:

- `gateway.env.example`

Ele descreve os parametros esperados pelo gateway.

Os principais sao:

- `GATEWAY_ID`
- `LOCAL_BROKER_HOST`
- `LOCAL_BROKER_PORT`
- `LOCAL_TOPIC_FILTER`
- `CLOUD_BROKER_HOST`
- `CLOUD_BROKER_PORT`
- `CLOUD_BROKER_USERNAME`
- `CLOUD_TOPIC`
- `CLOUD_PAYLOAD_FORMAT`
- `SQLITE_PATH`
- `FORWARD_INTERVAL_SECONDS`
- `PUBLISH_QOS`

O arquivo real `gateway.env` e tratado como configuracao local de execucao e nao fica versionado.

## Execucao

### Execucao manual

Fluxo basico:

1. instalar as dependencias Python
2. criar um `gateway.env` a partir de `gateway.env.example`
3. garantir que o broker MQTT local esteja ativo
4. executar:

```bash
python3 -m gateway.main
```

### Execucao como servico

Na TV box, o gateway foi preparado para ser executado com `systemd`.

Com isso, o servico pode:

- subir junto com o sistema
- ser monitorado pelo init
- ser controlado com `systemctl`

## Decisoes de Implementacao

### Broker local separado do forwarder

O broker local e o forwarder foram mantidos como componentes distintos.

Isso deixa a arquitetura mais clara:

- o broker recebe e distribui mensagens
- o gateway interpreta e encaminha os dados

### Outbox dupla no sistema completo

O sistema completo tem duas camadas de persistencia:

- uma outbox no app de borda
- uma outbox no gateway da TV box

Isso aumenta a tolerancia a falhas em mais de um ponto do fluxo.

### Compatibilidade com o app de borda atual

O gateway foi adaptado ao formato ja existente no app de borda.

Isso inclui:

- escuta no topico `telemetry`
- leitura de `device_id` a partir do JSON do payload

Essa escolha permitiu validar o fluxo real sem exigir refatoracao imediata do app Android.

## Resumo

Esta pasta implementa a parte do gateway do projeto.

Ela transforma a TV box em um ponto de agregacao local para multiplos dispositivos de borda, com capacidade de:

- receber dados via MQTT
- persistir localmente
- encaminhar para a nuvem
- continuar operando mesmo diante de falhas temporarias de conectividade
