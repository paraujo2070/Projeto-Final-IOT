package br.com.pedro_araujo.coleta_de_dados.ml

import android.content.Context
import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val assetManager = context.assets
            val modelPath = "modelo/modelo_invasao.onnx"
            
            // Listar arquivos no diretório para debug
            Log.d("InferenceManager", "📂 Verificando pasta assets/modelo...")
            assetManager.list("modelo")?.forEach { 
                Log.d("InferenceManager", "  - Arquivo encontrado: $it")
            } ?: Log.e("InferenceManager", "❌ Pasta 'modelo' não encontrada nos assets!")

            val modelBytes = assetManager.open(modelPath).readBytes()
            session = env.createSession(modelBytes)
            Log.i("InferenceManager", "✅ Modelo ONNX carregado com sucesso ($modelPath)")
        } catch (e: Exception) {
            Log.e("InferenceManager", "❌ Erro ao carregar modelo ONNX: ${e.message}")
            e.printStackTrace()
        }
    }

    fun runInference(inputs: FloatArray): String {
        val session = session ?: return "Erro: Modelo não carregado"
        
        try {
            val inputName = session.inputNames.iterator().next()
            val shape = longArrayOf(1, inputs.size.toLong())
            val floatBuffer = FloatBuffer.wrap(inputs)
            
            OnnxTensor.createTensor(env, floatBuffer, shape).use { inputTensor ->
                session.run(mapOf(inputName to inputTensor)).use { result ->
                    val output = result[0].value
                    return processOutput(output)
                }
            }
        } catch (e: Exception) {
            Log.e("InferenceManager", "❌ Erro na inferência: ${e.message}")
            return "Erro na Inferência"
        }
    }

    private fun processOutput(output: Any): String {
        return when (output) {
            is LongArray -> if (output[0] == 1L) "Invasão!" else "Normal"
            is FloatArray -> if (output[0] > 0.5) "Invasão!" else "Normal"
            is Array<*> -> {
                val firstElement = output[0]
                if (firstElement is FloatArray) {
                    if (firstElement[0] > 0.5) "Invasão!" else "Normal"
                } else firstElement.toString()
            }
            else -> output.toString()
        }
    }
}
