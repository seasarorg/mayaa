---
layout: base
title: お知らせ
eleventyNavigation:
  key: News
  title: お知らせ
  order: 2
---

## {{ title }}

{% assign news = collections.news | reverse %}
<uL>
{%- for post in news limit:30 -%}
<li>
  <a href="{{ post.url }}" class="text-sm text-black no-underline"><div class="">{{ post.data.title }}</div></a><span class="text-gray-500">({{ post.data.date | date: '%Y.%m.%d' }})</span>
</li>
{%- endfor -%}
</ul>
