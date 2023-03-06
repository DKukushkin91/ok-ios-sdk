Pod::Spec.new do |s|
  s.name         = "ok-ios-sdk"
  s.version      = "2.0.14"
  s.summary      = "iOS library for working with OK API."
  s.platform     = :ios, "12.4"
  s.source       = { :git => "https://github.com/Dkukushkin91/ok-ios-sdk.git", :tag => s.version.to_s }
  s.source_files = '*.{h,m}'
  s.public_header_files = "*.h"
  s.requires_arc = true
end
