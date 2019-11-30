require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version'].gsub(/v|-beta/, '')
  s.summary        = package['description']
  s.author         = package['author']
  s.license        = package['license']
  s.homepage       = package['homepage']
  s.source         = { :git => 'https://github.com/yuntitech/react-native-yunti-clickread.git', :tag => "v#{s.version}"}
  s.ios.deployment_target = '10.0'
  s.preserve_paths = '*.js'
  s.source_files = 'ios/**/*.{h,m}'

  s.dependency "React-Core"
end
