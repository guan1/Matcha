//
//  HttpAction.swift
//  Vicky
//
//  Created by Andre Guggenberger on 17/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation

open class Action : NSObject, NSCopying {
    
    var name : String
    var element : String?
    var parameters : [String : Any]?
    var line : UInt = 0
    
    var wait : TimeInterval = 2.0
    var pollInterval : Float = 0.25
    
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        self.name = name
        self.element = firstParameter
        if parameters.isEmpty == false {
            self.parameters = parameters
        }
        self.line = line
    }
    
    class func createAction(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) -> Action {
        if name == "scroll" {
            return ScrollAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "enter" {
            return EnterAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "verify" {
            if parameters["navigationTitle"] != nil {
                return VerifyNavigationAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
            } else {
                return VerifyAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
            }
        }
        if name == "contains" {
            return VerifyAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "executeJS" {
            return ExecuteJSAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "http" {
            return HttpAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "wait" {
            return WaitAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        if name == "searchField" {
            return SearchFieldAction(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
        }
        return Action(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }    
    
    open func copy(with zone: NSZone? = nil) -> Any {
        let params = self.parameters != nil ? self.parameters! : [:]
        return type(of:self).init(name: self.name, firstParameter: self.element, parameters: params, line: self.line)
    }
}

open class HttpAction : Action {
    var url : String
    var header: String?
    var params: String?
    
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        self.url = parameters["url"] as! String
        self.header = parameters["headers"] as? String
        self.params = parameters["params"] as? String
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class ScrollAction : Action {
    var to : String
    var direction : String = "right"
    var amount : Float = 500
    
   required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        self.to = parameters["to"] as! String
        if parameters["direction"] != nil {
            self.direction = parameters["direction"] as! String
        }
        if parameters["amount"] != nil {
            self.amount = Float(parameters["amount"] as! String)!
        }
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class ExecuteJSAction : Action {
    var code : String
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        if parameters["value"] != nil {
            self.code = parameters["value"] as! String
        } else {
            self.code = parameters["code"] as! String
        }
        
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class VerifyNavigationAction : Action {
    var navigationTitle : String
    
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        if parameters["value"] != nil {
            self.navigationTitle = parameters["value"] as! String
        } else {
            self.navigationTitle = parameters["navigationTitle"] as! String
        }
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class VerifyAction : Action {
    var value : String?
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        self.value = parameters["value"] as? String
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class EnterAction : Action {
    var value : String?
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        self.value = parameters["value"] as? String
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class WaitAction : Action {
    var value : TimeInterval = 0.5
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        if parameters["value"] != nil {
            self.value = parameters["value"] as! TimeInterval
        } else if let firstParameter = firstParameter {
            self.value = TimeInterval(firstParameter)!
        }
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)
    }
}

open class SearchFieldAction : Action {
    var value : String = ""
    required public init(name: String, firstParameter: String?, parameters: [String : Any], line: UInt) {
        if parameters["value"] != nil {
            self.value = parameters["value"] as! String
        } else if let firstParameter = firstParameter {
            self.value = firstParameter
        }
        super.init(name: name, firstParameter: firstParameter, parameters: parameters, line: line)                
    }
}
