//
//  TestRunner.swift
//  Vicky
//
//  Created by Andre Guggenberger on 10/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation
import XCTest
import EarlGrey

@objc
public protocol EarlGreyTestRunnerDelegate {
    func getSearchBar(_ action: SearchFieldAction) -> UISearchBar?
}

open class EarlGreyTestRunner : NSObject {
    var testCasePath : String?
    
    private var testCase: TestCase
    weak private var xcTestCase : XCTestCase?
    
    weak private var delegate : EarlGreyTestRunnerDelegate?
    
    private var currentRunningScenario : Scenario?
    private var currentRunningAction : Action?
    
    init(testCase: TestCase, xcTestCase: XCTestCase, delegate: EarlGreyTestRunnerDelegate) {
        self.testCase = testCase
        self.xcTestCase = xcTestCase
        self.delegate = delegate
    }
    
    func run() {
        for scenario in testCase.scenarios {
            currentRunningScenario = scenario
            for precondition in scenario.preconditions {
                currentRunningAction = precondition
                performAction(action:precondition)
            }
            
            for action in scenario.steps {
                currentRunningAction = action
                performAction(action: action)
            }
        }
    }
    
    private func performAction(action: Action) {
        let funcName = String(format: "%@:", action.name)
        let selector = NSSelectorFromString(funcName)
        
        if xcTestCase != nil && xcTestCase!.responds(to: selector) {
            _ = xcTestCase?.perform(selector, with: action)
        } else {
            self.perform(selector, with: action)
        }
    }
    
    private func handleError(error: NSError?) {
        if let error = error {
            let message = error.localizedFailureReason != nil ? error.localizedFailureReason! : error.localizedDescription
            var fileName:NSString = ""
            if let testCasePath = testCasePath {
                let path : NSString = (testCasePath as NSString).deletingLastPathComponent as NSString
                fileName = "\(path)/TestCases/\(testCase.fileName)" as NSString
            }
            if let currentRunningAction = currentRunningAction {
                xcTestCase?.recordFailure(withDescription: message, inFile: fileName as String, atLine: currentRunningAction.line, expected: true)
                print("TestCase \(testCase.fileName) failed at line \(currentRunningAction.line).\nMessage: \(message)")
            } else {
                xcTestCase?.recordFailure(withDescription: message, inFile: fileName as String, atLine: 0, expected: true)
                print("TestCase \(testCase.fileName) failed.\nMessage: \(message)")
            }
        }
    }
    
    open func enter(_ action : EnterAction) {
        let elementId = action.element!
        let text = action.value!
        var error: NSError?
        MatchaEarlGrey.select(elementWithMatcher: grey_accessibilityID(elementId)).perform(grey_typeText(text), error: &error)
        
        handleError(error: error)
    }
    
    open func contains(_ action: VerifyAction) {
        verify(action, contains: true)
    }
    
    open func verify(_ action: Action) {
        if action.isKind(of: VerifyNavigationAction.self) {
            verifyNavigationTitle(action as! VerifyNavigationAction)
        } else {
            verify(action as! VerifyAction, contains: false)
        }
    }
    
    private func verify(_ action: VerifyAction, contains: Bool) {
        let elementId = action.element!
        
        var error: NSError?
        if let value = action.value {
            let textComparisonAssertion = { (expectedText: String) -> GREYAssertionBlock in
                return GREYAssertionBlock.assertion(withName: "Verify",
                                                    assertionBlockWithError: {
                                                        (element: Any?, errorOrNil: UnsafeMutablePointer<NSError?>?) -> Bool in
                                                        if let label = element as? UILabel {
                                                            if contains {
                                                                return label.text?.contains(value) ?? false
                                                            } else {
                                                                return label.text == value
                                                            }
                                                        }
                                                        if let textView = element as? UITextView {
                                                            if contains {
                                                                return textView.text?.contains(value) ?? false
                                                            } else {
                                                                return textView.text == value
                                                            }
                                                        }
                                                        let errorInfo = [NSLocalizedDescriptionKey: NSLocalizedString("Element is not of correct type: \(element.debugDescription)", comment: "")]
                                                        errorOrNil?.pointee = NSError(domain: kGREYInteractionErrorDomain, code: 2, userInfo: errorInfo)
                                                        return false
                })
            }
            MatchaEarlGrey.select(elementWithMatcher: grey_accessibilityID(elementId)).assert(textComparisonAssertion(value), error: &error)
        } else {            
            MatchaEarlGrey.select(elementWithMatcher: grey_accessibilityID(elementId)).assert(with: grey_notNil(), error: &error)
            
        }
        
        handleError(error: error)
    }
    
    open func url(_ action: HttpAction) {
        //TODO or let sub classes do this
    }
    
    open func searchField(_ action: SearchFieldAction) {
        let searchBar = getSearchBar(action)
        if let searchBar = searchBar {
            searchBar.matcha_searchBarTextField()?.becomeFirstResponder()
            EarlGreyTestRunner.waitForCompletion(timeout: action.wait, pollInterval: TimeInterval(action.pollInterval))
        
            searchBar.matcha_searchBarTextField()?.text = action.value
            searchBar.delegate?.searchBar!(searchBar, textDidChange: action.value)
        
            EarlGreyTestRunner.waitForCompletion(timeout: action.wait, pollInterval: TimeInterval(action.pollInterval))
        
            searchBar.delegate?.searchBarCancelButtonClicked?(searchBar)
        
            EarlGreyTestRunner.waitForCompletion(timeout: action.wait, pollInterval: TimeInterval(action.pollInterval))
        } else {
            handleError(error: NSError(domain: "No search bar found", code: 0, userInfo: nil))
            XCTFail("No search bar found")
        }
    }
    
    open func getSearchBar(_ action: SearchFieldAction) -> UISearchBar? {
        return delegate?.getSearchBar(action)
    }
    
    open func click(_ action: Action) {
        let elementId = action.element!
        var error: NSError?
        MatchaEarlGrey.select(elementWithMatcher: grey_accessibilityID(elementId)).perform(grey_tap(), error: &error)
        handleError(error: error)
        
        EarlGreyTestRunner.waitForCompletion(timeout: action.wait, pollInterval: TimeInterval(action.pollInterval))
    }
    
    open func wait(_ action: WaitAction) {
        let condition = GREYCondition(name: "waitCondition") { () -> Bool in
            return false
        }
        let _ = condition?.wait(withTimeout: CFTimeInterval(action.value), pollInterval: CFTimeInterval(action.value))
    }
    
    open func scroll(_ action: ScrollAction) {
        let scrollingElement = action.element!
        let searchElement = action.to
        let direction = action.direction
        let amount = action.amount
        
        var greyDirection = GREYDirection.down
        if direction == "up" {
            greyDirection = .up
        } else if direction == "down" {
            greyDirection = .down
        } else if direction == "right" {
            greyDirection = .right
        } else if direction == "left" {
            greyDirection = .left
        }
        
        var error: NSError?
        MatchaEarlGrey
            .select(elementWithMatcher:grey_accessibilityID(searchElement))
            .using(searchAction: grey_scrollInDirection(greyDirection, CGFloat(amount)), onElementWithMatcher: grey_accessibilityID(scrollingElement))
            .assert(grey_notNil(), error: &error)
        handleError(error: error)
    }

    
    open func verifyNavigationTitle(_ action: VerifyNavigationAction) {
        let text = action.navigationTitle
        let appDelegate = UIApplication.shared.delegate
        if let appDelegate = appDelegate, let window = appDelegate.window {
            let viewController = window!.rootViewController?.matcha_getVisibleViewController()
            if let title = viewController?.title {
                if text != title {
                    handleError(error: NSError(domain: "Wrong navigation title. Expected: \(text), Actual: \(title)", code: 0, userInfo: nil))
                }
                XCTAssertEqual(text, title)
            }
        }
    }
    
    open func executeJS(_ action: ExecuteJSAction) {
        let webViewId = action.element!
        
        let js = action.code
        
        var error: NSError?
        let jsAction = grey_javaScriptExecution(js, nil)
        MatchaEarlGrey.select(elementWithMatcher: grey_accessibilityID(webViewId)).perform(jsAction, error: &error)
        handleError(error: error)
    } 
    
    open func back(_ action: Action) {
        let appDelegate = UIApplication.shared.delegate
        if let appDelegate = appDelegate, let window = appDelegate.window {
            let viewController = window!.rootViewController?.matcha_getVisibleViewController()
            viewController?.navigationController?.popViewController(animated: true)
            
            waitForCompletion(action: action)
        }
    }

    private func waitForCompletion(action: Action) {
        let condition = GREYCondition(name: "condition") { () -> Bool in
            return false
        }
        let _ = condition?.wait(withTimeout: action.wait, pollInterval: CFTimeInterval(action.pollInterval))
    }
    
    open class func waitForCompletion(timeout: TimeInterval, pollInterval: TimeInterval, completionBlock: (() -> Bool)? = nil) {
        let condition = GREYCondition(name: "condition") { () -> Bool in
            if let completionBlock = completionBlock {
                return completionBlock()
            } else {
                return false
            }
        }
        let _ = condition?.wait(withTimeout: timeout, pollInterval: CFTimeInterval(pollInterval))
    }

}
