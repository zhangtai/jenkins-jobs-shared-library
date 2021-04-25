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
    // Add attributes
    def attributes = []
    modelDef?.attrs?.each { Map attrDef ->
        println "Creating attribute: ${attrDef}"
        Map controlDef = attrDef.control as Map
        def control = null
        if (controlDef.type == "TextFieldControl") {
            control = new TextFieldControl()
        }
        if (controlDef.type == "ChoiceControl") {
            def options = controlDef.options.collect { Map option ->
                new ChoiceControl.Option(option.display as String, option.value as String)
            }
            Map modes = [
                    "DROPDOWN_LIST": ChoiceControl.Mode.DROPDOWN_LIST,
                    "RADIO_BUTTON": ChoiceControl.Mode.RADIO_BUTTON
            ]
            control = new ChoiceControl(options, modes.get(controlDef.mode))
        }
        def attr = new Attribute(attrDef.name as String, attrDef.displayName as String, control)
        attributes << attr
    }
    println "Setting attributes"
    auxModel.setAttributes(attributes)
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
