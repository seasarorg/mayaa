const { EleventyHtmlBasePlugin } = require("@11ty/eleventy");
const eleventyNavigationPlugin = require("@11ty/eleventy-navigation");

const markdownIt = require("markdown-it");
const markdownItPrism = require('markdown-it-prism');
const markdownItAttrs = require('markdown-it-attrs');
const markdownItContainer = require('markdown-it-container');
const markdownItDefList = require('markdown-it-deflist');

module.exports = function(eleventyConfig) {
  eleventyConfig.addWatchTarget('./tailwind.config.js')
  eleventyConfig.addWatchTarget('./src/css/tailwind.css')

  eleventyConfig.addPassthroughCopy("./src/**/*.{jpg,png,gif,svg,ico}");
  eleventyConfig.addPassthroughCopy({ "./node_modules/flowbite/dist/*.js": "./js/" });
  eleventyConfig.addPassthroughCopy({ "./node_modules/prismjs/themes/*.css": "./css/prismjs/" });
  eleventyConfig.addPassthroughCopy("./src/about/slide/*.pdf");

  eleventyConfig.addPlugin(EleventyHtmlBasePlugin);
  eleventyConfig.addPlugin(eleventyNavigationPlugin);

  let options = {
    html: true,
    breaks: false,
    linkify: true
  };
  eleventyConfig.setLibrary("md", markdownIt(options));
  eleventyConfig.amendLibrary("md", mdLib => 
    mdLib
    .use(markdownItAttrs)
    .use(markdownItDefList)
    .use(markdownItPrism)
    .use(markdownItContainer, 'note', {
      render: function (tokens, idx) {
        var m = tokens[idx].info.trim().match(/^note\s+(.*)$/);
        if (tokens[idx].nesting === 1) {
          // opening tag
          return `<section class="callout callout-${mdLib.utils.escapeHtml(m[1])}">`;
        } else {
          // closing tag
          return '</section>';
        }
      }
    })
  );

  eleventyConfig.addShortcode("proc", function(processorName) {
    return `<a href="/docs/processors/#${processorName.replace(/m:/g, '')}"><code>${processorName}</code></a>`;
  });

  eleventyConfig.setLiquidOptions({
    jsTruthy: true,
    dynamicPartials: false,
    strictFilters: false, // renamed from `strict_filters` in Eleventy 1.0
  });
  return {
    dir: {
      input: "src",
    }
  };
};