//
//  BaseTests.swift
//  Vicky
//
//  Created by Andre Guggenberger on 10/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation
import XCTest

class EarlGreyTests : XCTestCase, EarlGreyTestRunnerDelegate {
    private var fileName : String!
    
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }
    
    override func tearDown() {
        super.tearDown()
    }
    
    func performTests(fileName: String, testCasePath: String? = nil) {
        self.fileName = fileName
        let scanner = Scanner(fileName: fileName)
        if let testCase = scanner.testCase {
            let testRunner = EarlGreyTestRunner(testCase: testCase, xcTestCase: self, delegate: self)
            testRunner.testCasePath = testCasePath
            testRunner.run()
        }
    }
    
    func waitForCompletion(timeout: TimeInterval, pollInterval: TimeInterval, completionBlock: (() -> Bool)? = nil) {
        EarlGreyTestRunner.waitForCompletion(timeout: timeout, pollInterval: pollInterval, completionBlock: completionBlock)
    }
    
    func getSearchBar(_ action: SearchFieldAction) -> UISearchBar? {
        XCTFail("subclasses have to override this method!")
        return nil
    }

}
