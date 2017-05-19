Pod::Spec.new do |s|

  s.name         = "Matcha"
  s.version      = "0.0.1"
  s.summary      = "A DSL for automated UI tests using EarlGrey"

  s.description  = <<-DESC
				A DSL for automated UI tests using EarlGrey ... TODO: more text coming
                   DESC

  s.homepage     = "https://github.com/guan1/Matcha"
	s.license      = { :type => "MIT", :file => "LICENSE" }

  s.author             = { "Andre Guggenberger" => "andre.guggenberger@caseapps.at" }
	s.platform     = :ios, '9.0'
	
	s.xcconfig       = { 'FRAMEWORK_SEARCH_PATHS' => '"$(PODS_ROOT)/"' }

	s.source = { :git => "https://github.com/guan1/Matcha.git", :commit => "d6d159bd661a934c8b305156093c307d13f8448b" }		
		s.source_files  = "iOS/Matcha/Matcha/Sources/*.*"
#	s.source       = { :http => "https://dl.dropboxusercontent.com/u/5810798/Matcha/x.zip"}
# s.source_files = '*.swift', '*.h', '*.m'
	s.ios.dependency  'EarlGrey'

	s.frameworks = 'XCTest'
end
