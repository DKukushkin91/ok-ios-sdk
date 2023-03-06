require "json"
package = JSON.parse(File.read(File.join(__dir__, '../', 'package.json')))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package["version"]
  s.summary       = package['description']
  s.homepage      = package["homepage"]
  s.license       = package["license"]
  s.requires_arc  = true
  s.author        = { 'DKukushkin' => 'DKukushkin@gmail.com' }
  s.source        = { :git => 'https://github.com/Dkukushkin91/ok-ios-sdk.git', :tag => s.version.to_s }

  s.source_files  = 'ios/*.{h,m}'

  s.platform     = :ios, "12.4"

  s.dependency 'React'

  s.subspec 'ok-ios-sdk' do |ss|
    ss.dependency     'ok-ios-sdk'
    ss.source_files = '*.{h,m}'
  end
end
