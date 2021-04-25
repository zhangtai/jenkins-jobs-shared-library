import groovy.transform.Field

import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.modeling.impl.auxiliary.AuxModel
import com.cloudbees.hudson.plugins.modeling.Attribute
import com.cloudbees.hudson.plugins.modeling.controls.*

@Field Jenkins jenkins = Jenkins.get()

def createModel(Map modelDef, String superType = null) {
    println "Creating model ${modelDef}, superType is: ${superType}"
    def auxModel = new AuxModel(jenkins, modelDef.name as String)
    if (superType) {
        println "Setting Super Type: ${superType}"
        auxModel.setSuperTypeId(superType)
    }
    println "Saving model ${auxModel}"
    auxModel.save()
    jenkins.save()
    jenkins.reload()
    if (modelDef?.models) {
        println "Creating nexted models: ${modelDef.models}"
        modelDef.models.each { childModelDef ->
            createModel(childModelDef as Map, modelDef.name as String)
        }
    }
}

def createAllModels() {
    Map auxDefs = readYaml(text: libraryResource("aux-models.yaml") as String) as Map
    auxDefs.models.each { module ->
        createModel(module as Map)
    }
}
