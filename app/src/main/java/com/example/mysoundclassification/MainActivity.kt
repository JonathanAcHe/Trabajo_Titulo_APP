// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.mysoundclassification

import android.Manifest
import android.os.Bundle
import android.os.Vibrator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"

    // TODO 2.1: defines the model to be used
    var modelPath = "lite-model_yamnet_classification_tflite_1.tflite"

    // TODO 2.2: defining the minimum threshold
    var probabilityThreshold: Float = 0.3f

    lateinit var textView: TextView

    fun vibratePhone(tipo: String): String {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        //val pattern = longArrayOf(0, 100, 1000, 200, 2000)
        if (tipo == "Dog")
            //vibrator.vibrate(pattern,-1)
            vibrator.vibrate(100) // 500 es mucho, 150 es como un pulso de medio segundo

        if (tipo == "Crying")
            vibrator.vibrate(300) // 500 es mucho, 150 es como un pulso de medio segundo
        return tipo
    }

    //Esta funciona
    //fun vibratePhone() {
    //    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    //    vibrator.vibrate(300) // 500 es mucho, 150 es como un pulso de medio segundo
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val REQUEST_RECORD_AUDIO = 1337
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)

        textView = findViewById<TextView>(R.id.output)
        val recorderSpecsTextView = findViewById<TextView>(R.id.textViewAudioRecorderSpecs)

        // TODO 2.3: Loading the model from the assets folder
        val classifier = AudioClassifier.createFromFile(this, modelPath)

        // TODO 3.1: Creating an audio recorder
        val tensor = classifier.createInputTensorAudio()

        // TODO 3.2: showing the audio recorder specification
        val format = classifier.requiredTensorAudioFormat
        val recorderSpecs = "Number Of Channels: ${format.channels}\n" +
                "Sample Rate: ${format.sampleRate}"
        recorderSpecsTextView.text = recorderSpecs

        // TODO 3.3: Creating
        val record = classifier.createAudioRecord()
        record.startRecording()

        Timer().scheduleAtFixedRate(1, 500) { //ORIGINALMENTE EL PERIODO ESTABA EN 500 haciendo pruebas con 4500

            // TODO 4.1: Classifing audio data
            val numberOfSamples = tensor.load(record)
            val output = classifier.classify(tensor)

            // TODO 4.2: Filtering out classifications with low probability
            val filteredModelOutput = output[0].categories.filter {
                it.score > probabilityThreshold
            }

            // TODO 4.3: Creating a multiline string with the filtered results
            val outputStr = filteredModelOutput.sortedBy { -it.score }
                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }

            // TODO 4.4: Updating the UI // Aca se puede actualizar lo que muestra en pantalla

            if (outputStr.isNotEmpty())

                runOnUiThread {
                    textView.text = outputStr
                    println("outputStr: " + outputStr)
                    println("textView.text: " + textView.text)
                    if (textView.text.contains("Dog")){
                        vibratePhone("Dog")
                        println("********* IF DOG *********")
                    }else if (textView.text.contains("Cry")){
                        vibratePhone("Crying")
                        println("********* IF Crying *********")
                    }else if (textView.text.contains("Music")){
                        //vibratePhone("Crying")
                        println("********* IF Music *********")
                    }else {
                        println("********* NO ES NINGUN SONIDO REQUERIDO *********")
                        textView.text = "NO ES NINGUN SONIDO REQUERIDO"
                    }
                    
                }

                //vibratePhone()

        }
    }
}