#!/usr/bin/env python
import urllib
import sys
import re

url_base='https://sisweb.ucd.ie/usis/w_sm_web_inf_viewer_banner.show_search?p_search_by=SCHOOL&p_term_code=200700&p_search_all=Y&p_level0=Y&p_level1=Y&p_level2=Y&p_level3=Y&p_level4=Y&p_level5=Y&p_category='
tag='Search_Listing_Heading'
id=re.compile('>([A-Z][a-zA-Z0-9]*)\s*-\s*([^<]*)</A>')

for code in sys.argv[1:]:
  url=url_base+code
  page=0
  found=True
  while found:
    page=page+1
    found=False
    h=urllib.urlopen(url+'&p_current_page='+str(page))
    for l in h:
      if l.find(tag)!=-1:
        found=True
        m=id.search(l)
        if m:
          print code,m.group(1),'"'+m.group(2)+'"'
