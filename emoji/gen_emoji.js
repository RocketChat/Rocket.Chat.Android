var request = require('request');
var _ = require("underscore");
var fs = require("fs");

function gen(emojis) {
  // Generate Java mapping
  var mapping = _(emojis).map(function(data, shortname) {
      // Get codepoints
      var codepoints = _(data.unicode.split("-")).map(function (code) {
          return "0x" + code;
      });
      var out = [];
      out.push('_shortNameToUnicode.put("' + shortname + '", new String(new int[] {' + codepoints.join(',') + '}, 0, ' + codepoints.length + '));');

      _(data.aliases).each(function(alias){
          out.push('_shortNameToUnicode.put("' + alias.replace(/:([-+\w]+):/,"$1") + '", new String(new int[] {' + codepoints.join(',') + '}, 0, ' + codepoints.length + '));');
      });
      return out.join("\n        ");
  }).join("\n        ");

  // Generate Java class from template
  var input  = fs.readFileSync("./Emojione_template.java");
  var output = _(input.toString()).template()({ mapping: mapping });

  // Write Java class to file
  var output_path = "./Emojione.java";
  fs.writeFileSync(output_path, output);

  console.log("Generated " + output_path);
}

request('https://raw.githubusercontent.com/Ranks/emojione/master/emoji.json', function (error, response, body) {
  if (!error && response.statusCode == 200) {
    gen(JSON.parse(body));
  }
})
