//
//  Testcase.swift
//  Vicky
//
//  Created by Andre Guggenberger on 10/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation

class Scenario {
    var name : String
    var preconditions:[Action] = []
    var steps:[Action] = []
    
    init(name: String) {
        self.name = name
    }
}
