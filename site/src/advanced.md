---
layout: base
title: 高度な使い方
eleventyNavigation:
  key: 高度な使い方
  order: 4
  nopage: false
tags: tutorial
---

## {{ title }}

{% assign navPages = collections.all | eleventyNavigation %}
{%- for entry in navPages %}
  {%- if entry.url == "/advanced/" %}<!-- 高度な使い方の記事を一覧する -->
    {%- if entry.children.length > 0 %}
      {%- for post in entry.children %}

* [{{ post.title }}]({{ post.url }})<br>
{%- if post.description %}{{ post.description }}{%- endif %}

      {%- endfor %}
    {%- endif %}
  {%- endif %}
{%- endfor %}
  </ul>
