//
//  Scanner.swift
//  Vicky
//
//  Created by Andre Guggenberger on 10/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation

public class Scanner {
    var testCase : TestCase?
    
    init(fileName: String) {
        
        if let path = Bundle(for: type(of: self)).path(forResource: fileName, ofType: nil) {
            do {
                let data = try String(contentsOfFile: path, encoding: .utf8)
                let lines = data.components(separatedBy: .newlines)
                
                var scenarios:[Scenario] = []
                
                var currentScenario : Scenario!
                var currentPreconditions: [Action]?
                var currentSteps: [Action]?
                
                var currentParameterLines: String?
                var currentActionName : String!
                var currentFirstParameter : String?

                
                var lineNumber : UInt! = nil
                for line in lines {
                    let trimmedLine = line.trimmingCharacters(in: CharacterSet.whitespaces)
                    if lineNumber == nil {
                        lineNumber = 1
                    } else {
                        lineNumber = lineNumber + 1
                    }
                    
                    if currentParameterLines != nil {
                        currentParameterLines = "\(currentParameterLines!)\(trimmedLine)"
                        
                        if trimmedLine == "}" {
                            let parameters = parseParams(string: currentParameterLines!)
                            let action = Action.createAction(name: currentActionName, firstParameter: currentFirstParameter, parameters: parameters, line: lineNumber)
                            if currentPreconditions != nil {
                                currentPreconditions!.append(action)
                            } else if currentSteps != nil {
                                currentSteps!.append(action)
                            }
                            currentParameterLines = nil
                        }
                        continue
                    }
                    
                    let tcString = matches(match: "TestCase:", line: trimmedLine)
                    if let tcString = tcString {
                        testCase = TestCase(fileName: fileName, name: tcString)
                        continue
                    }
                    
                    let scenarioString = matches(match: "Scenario:", line: trimmedLine)
                    if let scenarioString = scenarioString {
                        if let currentPreconditions = currentPreconditions {
                            currentScenario.preconditions = currentPreconditions
                        }
                        
                        if let currentSteps = currentSteps {
                            currentScenario.steps = currentSteps
                        }
                        
                        currentScenario = Scenario(name: scenarioString)
                        scenarios.append(currentScenario)
                        continue
                    }
                    
                    if trimmedLine.hasPrefix("Preconditions:") {
                        if let currentSteps = currentSteps {
                            currentScenario.steps = currentSteps
                        }
                        currentSteps = nil
                        currentPreconditions = []
                        continue
                    }
                    
                    if trimmedLine.hasPrefix("Steps:") {
                        if let currentPreconditions = currentPreconditions {
                            currentScenario.preconditions = currentPreconditions
                        }
                        currentSteps = []
                        currentPreconditions = nil
                        continue
                    }
                    
                    if trimmedLine.hasPrefix("@") {
                        //Sample: @verify element { key : 'value' } -> [verify, "{key:value}"]
                        let actionAndParams = trimmedLine.matcha_splitStep()
                        
                        //Sample: verify
                        currentActionName = actionAndParams.0
                        
                        currentFirstParameter = nil
                        var value : String?
                        var parameters : [String: Any] = [:]

                        
                        if let paramsAsString = actionAndParams.1 {
                            var isMultiLineParams = false
                            if paramsAsString.hasPrefix("{") && paramsAsString.hasSuffix("}") == false {
                                /*multiline params; Sample: {
                                    key : 'value' 
                                 }
                                */
                                currentParameterLines = paramsAsString
                                continue
                            } else if paramsAsString.hasPrefix("{") && paramsAsString.hasSuffix("}") {
                                //Sample: { key : 'value' }
                                parameters = parseParams(string: paramsAsString)
                            } else if paramsAsString.matcha_isValue() {
                                //Sample: 'value'
                                value = paramsAsString.matcha_trimValue()
                            } else {
                                //Sample: element { key : 'value' }
                                let paramsArray = paramsAsString.matcha_splitToFirstSpace()
                                if paramsArray?.isEmpty == false {
                                    var i = 0
                                    for p1 in paramsArray! {
                                        if i == 0 {
                                            currentFirstParameter = p1
                                            i = i + 1
                                            continue
                                        }
                                        
                                        let trimmedP = p1.trimmingCharacters(in: CharacterSet.whitespaces)
                                        if trimmedP.hasPrefix("{") && trimmedP.hasSuffix("}") == false {
                                            /*multiline params; Sample: {
                                                key : 'value'
                                             }
                                             */
                                            currentParameterLines = trimmedP
                                            isMultiLineParams = true
                                            break
                                        } else if trimmedP.hasPrefix("{") && trimmedP.hasSuffix("}") {
                                            //Sample: element { key : 'value' }
                                            parameters = parseParams(string: trimmedP)
                                            
                                        } else if (trimmedP.hasPrefix("'") && trimmedP.hasSuffix("'")) || (trimmedP.hasPrefix("\"") && trimmedP.hasSuffix("\"")) {
                                            //Sample: element 'value'
                                            value = trimmedP.matcha_trimValue()
                                        }
                                        
                                        i = i + 1
                                    }
                                }
                            }
                            
                            if isMultiLineParams {
                                continue
                            }
                        }
                        
                        if let value = value {
                            parameters["value"] = value
                        }
                        
                        let action = Action.createAction(name: currentActionName, firstParameter: currentFirstParameter, parameters: parameters, line: lineNumber)
                        if currentPreconditions != nil {
                            currentPreconditions!.append(action)
                        } else if currentSteps != nil {
                            currentSteps!.append(action)
                        }
                        
                    }
                }
               
                if let currentPreconditions = currentPreconditions {
                    currentScenario.preconditions = currentPreconditions
                }
                
                if let currentSteps = currentSteps {
                    currentScenario.steps = currentSteps
                }
                testCase?.scenarios = scenarios
                
            } catch {
                print(error)                
            }
        }
    }
    
    //checks if line matches match and returns the 'value' -> if line is "Scenario: Test" and match = "Scenario:" this function returns "Test"
    private func matches(match : String, line: String) -> String? {
        if line.trimmingCharacters(in: CharacterSet.whitespaces).hasPrefix(match) {
            return line.substring(from: line.characters.index(line.startIndex, offsetBy: match.lengthOfBytes(using: String.Encoding.utf8))).trimmingCharacters(in: CharacterSet.whitespaces)
        } else {
            return nil
        }
    }
    
    // parses strings of format { p : v, p1 : 'v', p2 : "v"} and returns a map [ "p" : "v", "p1" : "v", "p2" : "v"]
    private func parseParams(string: String) -> [String: Any] {
        var key : String!
        var value : String!
        
        var currentToken : String = ""
        var parameters : [String: Any] = [:]
        for c in string.characters {
            if c == "{" {
                continue
            } else if c == ":" {
                key = currentToken.trimmingCharacters(in: CharacterSet.whitespaces)
                currentToken = ""
                continue
            } else  if c == "}" || c == "," {
                value = currentToken.matcha_trimValue()
                parameters[key] = value.trimmingCharacters(in: CharacterSet.whitespaces)
                currentToken = ""
                continue
            }
            currentToken = "\(currentToken)\(c)"
        }
        return parameters
        
    }
    

}

