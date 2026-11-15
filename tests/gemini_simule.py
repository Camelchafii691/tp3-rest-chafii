"""API Gemini locale simulée. Clé et modèle factices uniquement, port 18086."""
import json, re
from http.server import BaseHTTPRequestHandler, HTTPServer
class Handler(BaseHTTPRequestHandler):
 def do_POST(self):
  data=json.loads(self.rfile.read(int(self.headers['Content-Length'])))
  text=json.dumps(data,ensure_ascii=False)
  n=int(re.search(r'exactement (\d+)',text).group(1))
  lieu='Paris' if 'Paris' in text else 'Nice'
  if 'ErreurAmont' in text:
   self.send_response(400);self.end_headers();self.wfile.write(b'{"error":{"code":400,"message":"Test","status":"INVALID_ARGUMENT"}}');return
  reponse=json.dumps({'ville_ou_pays':lieu,'endroits_a_visiter':['Visite simulee '+str(i+1) for i in range(n)],'prix_moyen_repas':'20 EUR'})
  if 'JsonInvalide' in text: reponse='ceci est invalide'
  if 'MauvaisNombre' in text: reponse='{"ville_ou_pays":"Paris","endroits_a_visiter":[],"prix_moyen_repas":"20 EUR"}'
  body=json.dumps({'candidates':[{'content':{'parts':[{'text':reponse}],'role':'model'},'finishReason':'STOP'}],'usageMetadata':{'promptTokenCount':10,'candidatesTokenCount':20,'totalTokenCount':30}}).encode()
  self.send_response(200);self.send_header('Content-Type','application/json');self.end_headers();self.wfile.write(body)
HTTPServer(('127.0.0.1',18086),Handler).serve_forever()
