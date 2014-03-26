#!/usr/bin/env python
import sys
import re
import time
import random
from SOAPpy import WSDL

numre=re.compile('([0-9]*)');

wsdlfile='AWSECommerceService.wsdl'
apikey='0EJBYECT89MVR2D2RT82'

srv=WSDL.Proxy(wsdlfile)

def clean(s):
  r=''
  for c in s:
    if ('a'<=c and c<='z') or ('A'<=c and c<='Z'):
      r=r+c
    else:
      r=r+' '
  return r

def flat(s):
  if isinstance(s,unicode) or isinstance(s, str):
    return s
  r=s[0]
  for e in s[1:]:
    r=r+', '+e
  return r

def parseYear(ys):
  try:
    m=numre.search(ys)
    return int(m.group(1))
  except ValueError:
    return 0

def get(obj, attr, default):
  try:
    r=obj[attr]
    if isinstance(r,str):
      return unicode(r,'UTF-8','ignore')
    else:
      return r
  except AttributeError:
    return default
  except TypeError:
    return default

def awsSearch(s,m):
  cnt=0
  response=srv.ItemSearch(AWSAccessKeyId=apikey,
                          SearchIndex='Books',
                          Sort='relevancerank',
                          ResponseGroup='ItemAttributes,EditorialReview,Images',
                          Keywords=clean(s))
  result=[]
  for item in get(get(response, 'Items', None), 'Item', []):
    attr=get(item, 'ItemAttributes', None)
    si=get(item, 'SmallImage', None)
    lp=get(attr, 'ListPrice', None)
    er=get(get(item, 'EditorialReviews', None), 'EditorialReview', None)
    result.append({'isbn' : get(attr, 'EAN', get(attr, 'ISBN', get(item, 'ASIN', 'OOPS'))),
                   'old_isbn' : get(attr, 'ISBN', 'unknown'),
                   'title' : get(attr, 'Title', 'unknown'),
                   'author' : flat(get(attr, 'Author', 'unknown')),
                   'publisher' : get(attr, 'Manufacturer', 'unknown'),
                   'edition': get(attr, 'Edition', 'unknown'),
                   'year' : parseYear(get(attr, 'PublicationDate', '0')),
                   'price' : float(get(lp, 'Amount', '0.0')) / 100.0,
                   'description': get(er, 'Content', 'none'),
                   'imageURL' : get(si, 'URL', '../bookshop/missing.jpg')})
    cnt=cnt+1
    if cnt>=m:
      break
  return result

def say(s):
  print s.encode('UTF-8'),

def dump(b, attr):
  def w(a):
    say('"'+b[a].replace('"','\\"')+'"')
  w(attr[0])
  for a in attr[1:]:
    say(',')
    w(a)

seenbooks=set()
for line in sys.stdin:
  line=line.replace('"', '')
  arg=line.split()
  course=arg[1]
  query=flat(arg[2:])
  books=[]
  try:
    books=awsSearch(query, 5)
  except:
    print 'FAIL:',line
  for b in books:
    if not b['isbn'] in seenbooks:
      say('insert into Books values(')
      dump(b,['isbn','old_isbn','title','author','publisher','edition', 'imageURL'])
      say(','+str(b['year'])+','+str(b['price'])+');\n')
    seenbooks.add(b['isbn'])
    say('insert into BooksToCourses values("'+course+'",5,')
    dump(b,['isbn','description'])
    print ');'
  time.sleep(random.randint(5,15))
