# TV Box Gateway

Esta pasta contem a implementacao da TV Box no projeto de monitoramento IoT de imoveis fechados.

A TV Box funciona como um gateway local: ela recebe os dados enviados pelos dispositivos de borda, executa o processamento local, aplica a inferencia de invasao, calcula indicadores auxiliares e publica na nuvem apenas os dados necessarios para visualizacao e tomada de decisao.

## Papel da TV Box no Sistema

No sistema completo existem tres partes principais:

- App de borda: roda no dispositivo responsavel pela coleta dos sensores.
- TV Box gateway: recebe os dados da borda, processa localmente e envia para a nuvem.
- App do proprietario: consome os dados ja publicados na nuvem.

O papel da TV Box e ficar entre os dispositivos de borda e a plataforma em nuvem. Isso permite que a nuvem nao precise receber todos os dados brutos da coleta. Em vez disso, a TV Box envia um pacote mais limpo, com as informacoes finais que interessam ao app do proprietario e a apresentacao do projeto.

## Fluxo de Dados

O fluxo atual e:

1. O app de borda coleta dados de clima, pressao e vibracao.
2. O app de borda publica um JSON via MQTT para a TV Box.
3. O broker Mosquitto local da TV Box recebe a mensagem no topico `telemetry`.
4. O gateway Python escuta esse topico e salva a mensagem em uma outbox SQLite.
5. O gateway executa o processamento local da mensagem.
6. O modelo TFLite roda na TV Box para estimar a chance de invasao.
7. O gateway monta o payload final no formato esperado pela Ubidots.
8. A TV Box publica o resultado na nuvem via MQTT.
9. O app do proprietario consome os dados da Ubidots.

Esse desenho tambem ajuda na tolerancia a falhas. Se a nuvem estiver temporariamente indisponivel, a mensagem fica salva localmente na outbox e o gateway tenta reenviar depois.

## Arquitetura Resumida

```text
Dispositivo de borda
  App Android de coleta
  Sensores / dados ambientais / movimento
        |
        | MQTT local
        v
TV Box
  Mosquitto local
  Gateway Python
  SQLite outbox
  Inferencia TFLite
        |
        | MQTT internet
        v
Ubidots
  Device: teste_projeto_final
  Variaveis finais do sistema
        |
        v
App do proprietario
  Dashboard / alertas / visualizacao
```

## Estrutura da Pasta

```text
tvbox_gateway/
+-- gateway/
|   +-- __init__.py
|   +-- cloud_forwarder.py
|   +-- config.py
|   +-- inference.py
|   +-- local_subscriber.py
|   +-- main.py
|   +-- models.py
|   +-- storage.py
+-- mosquitto/
|   +-- mosquitto.conf
+-- systemd/
|   +-- tvbox-gateway.service
+-- gateway.env.example
+-- requirements.txt
+-- README.md
```

Arquivos de runtime, credenciais, banco local e configuracoes reais de execucao nao devem ser versionados. O arquivo `gateway.env.example` serve apenas como modelo.

## Principais Componentes

### `gateway/main.py`

E o ponto de entrada do servico Python.

Ele carrega as configuracoes, inicializa o armazenamento local, inicia o assinante MQTT local e executa ciclos periodicos de envio para a nuvem.

### `gateway/config.py`

Centraliza a leitura das variaveis de ambiente.

E nele que ficam os parametros usados pelo gateway, como broker local, broker da nuvem, topicos MQTT, caminho do banco SQLite, caminhos do modelo de ML e thresholds de decisao.

### `gateway/local_subscriber.py`

Responsavel por escutar o broker MQTT local.

Na implementacao atual, ele usa `mosquitto_sub` para assinar o topico `telemetry`. Quando uma mensagem chega, o modulo valida se o payload e JSON e salva a mensagem na outbox SQLite.

### `gateway/storage.py`

Implementa a outbox local usando SQLite.

A outbox guarda as mensagens recebidas antes do envio para a nuvem. Isso evita perda imediata de dados caso a internet ou a Ubidots estejam fora do ar.

Cada registro armazena:

- topico de origem;
- topico de destino na nuvem;
- payload recebido;
- QoS;
- horario de recebimento;
- quantidade de tentativas;
- ultimo erro de envio.

Quando o envio para a nuvem funciona, a mensagem e removida da outbox.

### `gateway/inference.py`

Responsavel pelo processamento local dos dados.

Esse modulo:

- extrai temperatura e umidade do JSON recebido;
- extrai as features usadas pelo modelo de invasao;
- normaliza as features usando o scaler em JSON;
- executa o modelo TFLite;
- calcula `intrusao_detectada`;
- calcula indicadores simples de risco de mofo;
- retorna o resultado final para envio a Ubidots.

### `gateway/cloud_forwarder.py`

Responsavel por enviar dados para a nuvem.

Ele le as mensagens pendentes da outbox, transforma o payload para o formato configurado e publica no broker MQTT da nuvem. No formato atual, `ubidots_inference`, ele nao envia todas as variaveis brutas. Ele envia apenas o resultado da inferencia e os dados finais que o app do proprietario deve consumir.

### `mosquitto/mosquitto.conf`

Configuracao do broker MQTT local da TV Box.

Configuracao atual:

```text
listener 1883 0.0.0.0
allow_anonymous true
```

Isso permite que os dispositivos na mesma rede publiquem mensagens MQTT na porta `1883` da TV Box.

### `systemd/tvbox-gateway.service`

Unidade usada para rodar o gateway como servico do sistema.

Na TV Box, isso permite:

- iniciar automaticamente no boot;
- reiniciar em caso de falha;
- acompanhar logs com `journalctl`;
- controlar o gateway com `systemctl`.

## Integracao com o App de Borda

O app de borda deve publicar os dados via MQTT para a TV Box.

Configuracao esperada no app de borda:

```text
Broker/IP: IP da TV Box
Porta: 1883
Topico: telemetry
```

Exemplo de IP usado em testes:

```text
192.168.1.48
```

O payload recebido pelo gateway precisa ser um JSON contendo, pelo menos, os blocos usados pelo processamento:

```json
{
  "metadados": {
    "device_id": "borda-01",
    "timestamp": "2026-06-27T17:00:00Z",
    "janela_amostragem_segundos": 10
  },
  "clima": {
    "temperatura_c": 25.4,
    "umidade_relativa_pct": 64.2
  },
  "pressao": {
    "barometro_hpa_media": 1013.2,
    "barometro_hpa_variancia": 0.01
  },
  "inercial_vibracao": {
    "accel_x_variancia": 0.02,
    "accel_y_variancia": 0.01,
    "accel_z_variancia": 0.03,
    "gyro_x_variancia": 0.01,
    "gyro_y_variancia": 0.01,
    "gyro_z_variancia": 0.02
  },
  "contexto": {
    "status_sistema": "coletando",
    "label_coleta": "quarto_aberto"
  }
}
```

As oito features usadas pelo modelo de invasao sao:

- `barometro_hpa_media`;
- `barometro_hpa_variancia`;
- `accel_x_variancia`;
- `accel_y_variancia`;
- `accel_z_variancia`;
- `gyro_x_variancia`;
- `gyro_y_variancia`;
- `gyro_z_variancia`.

Temperatura e umidade nao entram no modelo de invasao. Elas sao enviadas para a nuvem como dados brutos e tambem usadas no calculo simples de risco de mofo.

## Inferencia de Invasao na TV Box

A inferencia de invasao roda localmente na TV Box usando um modelo TFLite.

Arquivos esperados:

```text
Modelo_Machine_learn/modelo_dnn_invasao_tflite213.tflite
Modelo_Machine_learn/scaler_telemetria_tflite213.json
```

O modelo recebe as oito features de pressao e movimento, ja normalizadas pelo scaler. A saida do modelo e um valor entre `0` e `1`, publicado como `intrusao_confianca`.

Essa saida nao deve ser interpretada como acuracia do modelo. Ela e o score/probabilidade retornado pelo modelo para a classe de invasao.

A decisao binaria e feita pelo threshold configurado:

```text
INTRUSION_DECISION_THRESHOLD=0.45
```

Regra atual:

```text
intrusao_confianca >= 0.45 -> intrusao_detectada = 1
intrusao_confianca < 0.45  -> intrusao_detectada = 0
```

Se o modelo TFLite ou o scaler nao estiverem disponiveis, o gateway possui uma heuristica de emergencia baseada nas variancias de movimento. Esse caminho serve apenas como fallback para manter o sistema funcionando, nao como comportamento principal.

## Risco de Mofo

O risco de mofo ainda nao usa ML. Ele e calculado por regra simples na TV Box a partir da umidade.

Regra atual:

```text
umidade < 60% -> risco_mofo_codigo = 0 -> seguro
60% a 70%    -> risco_mofo_codigo = 1 -> alerta
umidade > 70% -> risco_mofo_codigo = 2 -> critico
```

Tambem e enviado o indicador `aceleracao_termica`.

Regra atual:

```text
20 C <= temperatura <= 30 C -> aceleracao_termica = 1
caso contrario              -> aceleracao_termica = 0
```

A ideia e indicar se a temperatura esta em uma faixa que pode favorecer o crescimento de mofo quando combinada com umidade elevada.

## Dados Enviados para a Ubidots

O gateway esta configurado para enviar o payload no formato:

```text
CLOUD_PAYLOAD_FORMAT=ubidots_inference
```

Nesse formato, a Ubidots nao recebe todos os dados brutos. Ela recebe as variaveis finais abaixo.

### `temperatura_c`

Temperatura bruta recebida do app de borda.

E enviada porque pode ser exibida diretamente no app do proprietario e tambem ajuda a interpretar risco ambiental.

### `umidade_relativa_pct`

Umidade bruta recebida do app de borda.

E enviada porque e importante para monitoramento ambiental e para a avaliacao de risco de mofo.

### `intrusao_detectada`

Resultado final da decisao de invasao.

Valores:

```text
0 -> invasao nao detectada
1 -> invasao detectada
```

Essa e a variavel mais direta para alertas no app do proprietario.

### `intrusao_confianca`

Score retornado pelo modelo TFLite para invasao.

Valores esperados:

```text
0.0 a 1.0
```

Quanto maior, mais o modelo esta inclinando para a classe de invasao. O threshold atual para transformar esse score em alerta e `0.45`.

### `risco_mofo_codigo`

Codigo numerico do risco de mofo.

Valores:

```text
0 -> seguro
1 -> alerta
2 -> critico
```

Essa variavel e adequada para regras simples de interface, como mudar cor, exibir aviso ou gerar prioridade no app.

### `aceleracao_termica`

Indicador binario de faixa termica favoravel ao mofo.

Valores:

```text
0 -> temperatura fora da faixa de aceleracao
1 -> temperatura entre 20 C e 30 C
```

Essa variavel nao substitui o risco de mofo. Ela complementa a leitura da umidade.

## Contexto Enviado com Cada Variavel

Cada variavel enviada para a Ubidots segue o formato:

```json
{
  "value": 25.4,
  "timestamp": 1782589200000,
  "context": {
    "device_id": "borda-01",
    "gateway_id": "tvbox-sala-01",
    "source_timestamp": "2026-06-27T17:00:00Z",
    "status_sistema": "coletando",
    "label_coleta": "quarto_aberto",
    "janela_amostragem_segundos": 10,
    "intrusion_source": "tflite",
    "mold_risk_label": "alerta"
  }
}
```

Campos principais do contexto:

- `device_id`: identifica qual dispositivo de borda gerou a leitura.
- `gateway_id`: identifica qual TV Box encaminhou a leitura.
- `source_timestamp`: timestamp original enviado pela borda.
- `status_sistema`: estado informado pelo app de borda.
- `label_coleta`: rotulo informado pela borda, util para testes e analise.
- `janela_amostragem_segundos`: tamanho da janela de coleta usada pela borda.
- `intrusion_source`: indica se a inferencia veio de `tflite` ou de fallback `heuristic`.
- `mold_risk_label`: versao textual do risco de mofo, como `seguro`, `alerta` ou `critico`.

## Orientacao para o App do Proprietario

O app do proprietario deve consumir os dados da Ubidots, nao diretamente da TV Box.

Device usado no projeto:

```text
teste_projeto_final
```

Variaveis recomendadas para a interface:

- `intrusao_detectada`: usar para alerta principal de invasao.
- `intrusao_confianca`: usar como detalhe tecnico ou indicador de intensidade da suspeita.
- `temperatura_c`: exibir como informacao ambiental.
- `umidade_relativa_pct`: exibir como informacao ambiental.
- `risco_mofo_codigo`: usar para status visual de risco de mofo.
- `aceleracao_termica`: usar como indicador complementar do risco ambiental.

Sugestao de interpretacao no app:

```text
intrusao_detectada = 1 -> mostrar alerta de possivel invasao
intrusao_detectada = 0 -> mostrar estado normal

risco_mofo_codigo = 0 -> ambiente seguro
risco_mofo_codigo = 1 -> atencao para umidade
risco_mofo_codigo = 2 -> risco critico de mofo
```

O app tambem pode usar o `context.device_id` para diferenciar leituras quando houver mais de um dispositivo de borda enviando dados para a mesma TV Box.

## Configuracao do Gateway

O arquivo de referencia e:

```text
gateway.env.example
```

O arquivo real usado na TV Box deve se chamar:

```text
gateway.env
```

Ele nao deve ser enviado ao Git, porque pode conter token da nuvem, usuario MQTT, senha e caminhos especificos da maquina.

Variaveis principais:

```text
GATEWAY_ID=tvbox-sala-01

LOCAL_BROKER_HOST=127.0.0.1
LOCAL_BROKER_PORT=1883
LOCAL_TOPIC_FILTER=telemetry

CLOUD_BROKER_HOST=industrial.api.ubidots.com
CLOUD_BROKER_PORT=1883
CLOUD_BROKER_USERNAME=<token-da-ubidots>
CLOUD_BROKER_PASSWORD=
CLOUD_TOPIC=/v1.6/devices/teste_projeto_final
CLOUD_PAYLOAD_FORMAT=ubidots_inference

SQLITE_PATH=/var/lib/tvbox-gateway/outbox.db
FORWARD_INTERVAL_SECONDS=5
PUBLISH_QOS=1

INFERENCE_MODEL_PATH=../Modelo_Machine_learn/modelo_dnn_invasao_tflite213.tflite
INFERENCE_SCALER_PATH=../Modelo_Machine_learn/scaler_telemetria_tflite213.json
INTRUSION_DECISION_THRESHOLD=0.45
INTRUSION_VARIANCE_THRESHOLD=0.5
```

Observacao: na Ubidots usada neste projeto, o token e usado como `username` MQTT. A senha pode ficar vazia.

## Dependencias

Dependencia Python versionada:

```text
paho-mqtt==2.1.0
```

Na TV Box tambem sao necessarios:

```text
mosquitto
mosquitto-clients
python3
tflite_runtime
numpy
libatlas3-base
```

O `libatlas3-base` fornece bibliotecas numericas usadas pelo `numpy` na arquitetura da TV Box.

## Execucao Manual

Na pasta `tvbox_gateway`, com o `gateway.env` configurado:

```bash
set -a
. gateway.env
set +a
python3 -m gateway.main
```

Esse modo e util para testes rapidos e depuracao.

## Execucao como Servico

Na TV Box, o gateway roda como servico `systemd`.

Comandos uteis:

```bash
sudo systemctl status tvbox-gateway --no-pager -l
sudo systemctl restart tvbox-gateway
sudo journalctl -u tvbox-gateway -f --no-pager
```

Para verificar se o modelo TFLite foi carregado corretamente, procure no log por:

```text
Loaded TFLite intrusion model
```

Se aparecer `Falling back to heuristic intrusion detection`, significa que o modelo ou alguma dependencia nao foi carregada e o gateway entrou no modo de emergencia.

## Como Testar

### 1. Testar se a TV Box recebe dados localmente

Na TV Box:

```bash
mosquitto_sub -h 127.0.0.1 -t telemetry -v
```

No app de borda, iniciar a coleta.

Resultado esperado: aparecer uma linha com o topico `telemetry` seguido do JSON enviado pelo app.

Se nada aparecer, o problema provavelmente esta entre o app de borda, a rede local e o broker MQTT da TV Box.

### 2. Testar o gateway em tempo real

Na TV Box:

```bash
sudo journalctl -u tvbox-gateway -f --no-pager
```

Ao iniciar a coleta no app de borda, o esperado e ver mensagens indicando que o gateway armazenou e encaminhou o dado.

Exemplo esperado:

```text
Stored inbound message id=...
Forwarded message id=... to cloud topic=/v1.6/devices/teste_projeto_final
```

### 3. Testar na Ubidots

Na Ubidots, abrir o device:

```text
teste_projeto_final
```

Verificar se as variaveis abaixo foram atualizadas:

```text
temperatura_c
umidade_relativa_pct
intrusao_detectada
intrusao_confianca
risco_mofo_codigo
aceleracao_termica
```

