//
//  UITestsExtensions.swift
//  Vicky
//
//  Created by Andre Guggenberger on 17/05/2017.
//  Copyright © 2017 CASEapps. All rights reserved.
//

import Foundation
import UIKit

//TODO: extension prefix

extension String {
    
    //@test something, something -> (test, "something something")
    public func matcha_splitStep() -> (String, String?) {
        let array = self.matcha_splitToFirstSpace()!
        let actionName = array[0].substring(from: array[0].characters.index(array[0].startIndex, offsetBy: "@".lengthOfBytes(using: String.Encoding.utf8)))
        if array.count > 1 {
            let actionParamsAsString = array[1].trimmingCharacters(in: CharacterSet.whitespaces)
            return (actionName, actionParamsAsString)
        }
        return (actionName, nil)
    }
    
    public func matcha_splitToFirstSpace() -> [String]? {
        return self.characters.split(separator: " ", maxSplits: 1, omittingEmptySubsequences: true).map(String.init)
    }
    
    //starts/ends with ' or "?
    public func matcha_isValue() -> Bool {
        let s = self.trimmingCharacters(in: CharacterSet.whitespaces)
        return (s.hasPrefix("'") && s.hasSuffix("'")) || (s.hasPrefix("\"") && s.hasSuffix("\""))
    }
    
    //remove leading/trailing spaces and ' or "
    public func matcha_trimValue() -> String {
        let s = self.trimmingCharacters(in: CharacterSet.whitespaces)
        if s.matcha_isValue() {
            let end = s.index(s.endIndex, offsetBy: -1)
            let start = s.index(s.startIndex, offsetBy: 1)
            let range = start..<end
            return s.trimmingCharacters(in: CharacterSet.whitespaces).substring(with: range)
        } else {
            return s
        }
    }
}

extension UIViewController {
    func matcha_getVisibleViewController() -> UIViewController? {
        return matcha_getVisibleViewController(self)
    }
    
    public func matcha_getVisibleViewController(_ rootViewController: UIViewController?) -> UIViewController? {
        guard let rootViewController = rootViewController else {
            return nil
        }
        
        if rootViewController.isKind(of: UINavigationController.self) {
            let navigationController = rootViewController as! UINavigationController
            return navigationController.viewControllers.last!
        }
        
        if rootViewController.isKind(of: UITabBarController.self) {
            let tabBarController = rootViewController as! UITabBarController
            if tabBarController.selectedViewController == nil {
                return tabBarController
            }
            return matcha_getVisibleViewController(tabBarController.selectedViewController)
        }
        
        if rootViewController.presentedViewController == nil {
            return rootViewController
        }
        return matcha_getVisibleViewController(rootViewController.presentedViewController)
    }
    
}

extension UISearchBar {
    public func matcha_searchBarTextField() -> UITextField? {
        for subView in self.subviews {
            for sView in subView.subviews {
                if let textField = sView as? UITextField {
                    return textField
                }                
            }
        }
        return nil
    }
    
    public func matcha_searchBarCancelButton() -> UIButton? {
        for subView in self.subviews {
            for sView in subView.subviews {
                if let textField = sView as? UITextField {
                    if let clearButton = textField.value(forKey: "clearButton") as? UIButton {
                        return clearButton;
                    }
                }
                
            }
        }
        return nil
    }
    
}

