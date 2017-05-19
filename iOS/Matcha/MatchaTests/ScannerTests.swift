//
//  ScannerTests.swift
//  Vicky
//
//  Created by Andre Guggenberger on 17/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation
import XCTest
@testable import Matcha

class ScannerTests : XCTestCase {
    
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }
    
    func testScanner() {
       let scanner = Scanner(fileName: "ScannerTests.tc")
        if let testCase = scanner.testCase {
            XCTAssertEqual("ScannerTests.tc", testCase.fileName)
            XCTAssertEqual("Scanner Tests", testCase.name)
            XCTAssertEqual(2, testCase.scenarios.count)
            let scenario1 = testCase.scenarios.first!
            XCTAssertEqual("Test Scenario 1", scenario1.name)            
            XCTAssert(scenario1.steps.count > 0)
            XCTAssertEqual(scenario1.preconditions.first!.name, "preconditionAction")
            XCTAssertEqual(4, scenario1.preconditions.first!.line)
            
            /*
            @http {
                url='url',
                headers = 'header1,header2',
                params = 'param1,param2'
            }
            */
            let httpAction1 = scenario1.steps.next() as! HttpAction
            XCTAssertEqual("http", httpAction1.name)
            XCTAssertEqual("url", httpAction1.url)
            
            //@verify element.id { value: 'Harald Schmidt'}
            let verify1 = scenario1.steps.next() as! VerifyAction
            XCTAssertEqual("verify", verify1.name)
            XCTAssertEqual("element.id", verify1.element)
            XCTAssertEqual("Harald Schmidt", verify1.value!)
            
            //@verify element.id 'Harald Schmidt'
            let verify2 = scenario1.steps.next() as! VerifyAction
            XCTAssertEqual("element.id", verify2.element)
            XCTAssertEqual("Harald Schmidt", verify2.value)
            
            //@verify element.id
            let verify3 = scenario1.steps.next() as! VerifyAction
            XCTAssertEqual("element.id", verify3.element)
            XCTAssertNil(verify3.value)
            
            //@scroll element.id { to: target.id, direction: right, amount: 500 }
            let scroll1 = scenario1.steps.next() as! ScrollAction
            XCTAssertEqual("element.id", scroll1.element)
            XCTAssertEqual("target.id", scroll1.to)
            XCTAssertEqual("right", scroll1.direction)
            XCTAssertEqual(1500.0, scroll1.amount)
            
            //@scroll element.id       { to: target.id, direction: left }
            let scroll2 = scenario1.steps.next() as! ScrollAction
            XCTAssertEqual("element.id", scroll2.element)
            XCTAssertEqual("target.id", scroll2.to)
            XCTAssertEqual("left", scroll2.direction)
            XCTAssertEqual(500.0, scroll2.amount)
            
            /*@scroll element.id {
                to: target.id,
                direction: left
            }*/
            let scroll3 = scenario1.steps.next() as! ScrollAction
            XCTAssertEqual("scroll", scroll3.name)
            XCTAssertEqual("element.id", scroll3.element)
            XCTAssertEqual("target.id", scroll3.to)
            XCTAssertEqual("left", scroll3.direction)
        
            
            //@executeJS element.id "documents.form"
            let executeJS1 = scenario1.steps.next() as! ExecuteJSAction
            XCTAssertEqual("executeJS", executeJS1.name)
            XCTAssertEqual("element.id", executeJS1.element)
            XCTAssertEqual("document.forms['credentials'].j_username.value = '309405863';", executeJS1.code)
            
            //@executeJS element.id { code = "documents.form" }
            let executeJS2 = scenario1.steps.next() as! ExecuteJSAction
            XCTAssertEqual("element.id", executeJS2.element)
            XCTAssertEqual("document.forms['credentials'].j_username.value = '309405863';", executeJS2.code)
            
            //@customElement
            let customElement1 = scenario1.steps.next()
            XCTAssertEqual("customElement",customElement1.name)
            XCTAssertNil(customElement1.parameters)
            
            //@customElement { param : value}
            let customElement2 = scenario1.steps.next()
            XCTAssertEqual("customElement",customElement2.name)
            XCTAssertEqual(1,customElement2.parameters!.count)
            
            //@click element.id
            let click1 = scenario1.steps.next()
            XCTAssertEqual("click", click1.name)
            XCTAssertEqual("element.id", click1.element)
            
            //@enter element.id 'value'
            let enter1 = scenario1.steps.next() as! EnterAction
            XCTAssertEqual("element.id", enter1.element)
            XCTAssertEqual("value", enter1.value)
            
            //@enter element.id  { value = 'value'  }
            let enter2 = scenario1.steps.next() as! EnterAction
            XCTAssertEqual("enter", enter2.name)
            XCTAssertEqual("element.id", enter2.element)
            XCTAssertEqual("value", enter2.value)
            
            //@back
            let back1 = scenario1.steps.next()
            XCTAssertEqual("back", back1.name)
            
            //@verify { navigationTitle : 'Dashboard' }
            let verify4 = scenario1.steps.next() as! VerifyNavigationAction
            XCTAssertEqual("Dashboard", verify4.navigationTitle)
            
            let contains1 = scenario1.steps.next() as! VerifyAction
            XCTAssertEqual("element.id", contains1.element)
            XCTAssertEqual("Harald Schneemann", contains1.value)
            
            let wait1 = scenario1.steps.next() as! WaitAction
            XCTAssertEqual("wait", wait1.name)
            XCTAssertEqual(0.5, wait1.value)
            
            let wait2 = scenario1.steps.next() as! WaitAction
            XCTAssertEqual("wait", wait2.name)
            XCTAssertEqual(2, wait2.value)
            
            let searchField1 = scenario1.steps.next() as! SearchFieldAction
            XCTAssertEqual("searchField", searchField1.name)
            XCTAssertEqual("Test", searchField1.value)
            
            let scenario2 = testCase.scenarios[1]
            XCTAssertEqual("Test Scenario 2", scenario2.name)
            XCTAssertEqual(scenario2.preconditions.first!.name, "preconditionAction")
            XCTAssertEqual(scenario2.steps[0].name, "verify")
            XCTAssertEqual(scenario2.steps[1].name, "back")
        } else {
            XCTFail("parsing failed - no features found")
        }
    }
}

private var associationKey: UInt8 = 0
extension Array {
    
    var _index: Int {
        get {
            if let l = objc_getAssociatedObject(self, &associationKey) as? Int {
                return l
            } else {
                return 0
            }
        }
        set {
            objc_setAssociatedObject(self, &associationKey, newValue, .OBJC_ASSOCIATION_RETAIN)
        }
    }
    
    mutating func next() -> Action {
        let action = self[self._index]
        self._index = self._index + 1
        return action as! Action
    }
    
    mutating func reset() {
        self._index = 0
    }
}

