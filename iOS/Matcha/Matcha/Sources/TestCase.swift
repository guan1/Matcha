//
//  Feature.swift
//  Vicky
//
//  Created by Andre Guggenberger on 10/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation

open class TestCase {
    var fileName : String
    var name : String
    var scenarios:[Scenario] = []
    
    init(fileName: String, name: String) {
        self.fileName = fileName
        self.name = name
    }
}
