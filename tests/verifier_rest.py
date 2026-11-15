"""Teste Payara configuré avec gemini_simule.py ; aucune requête réelle vers Google."""
from urllib.request import urlopen
from urllib.error import HTTPError
import json
import sys

base = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:18085/tp3-rest-chafii"
cases = [
    ("/api/guide/lieu/Nice", 200, 2),
    ("/api/guide/lieu/Paris?nb=4", 200, 4),
    ("/api/guide/lieu/Paris?nb=0", 400, None),
    ("/api/guide/lieu/Paris?nb=11", 400, None),
    ("/api/guide/lieu/Paris?nb=abc", 404, None),
    ("/api/guide/lieu/%20", 400, None),
    ("/api/guide/lieu/JsonInvalide", 502, None),
    ("/api/guide/lieu/MauvaisNombre", 502, None),
    ("/api/guide/lieu/ErreurAmont", 502, None),
    ("/api/hello-world/personnes/Mohamed", 500, None),
]
results = []
for path, expected, count in cases:
    try:
        response = urlopen(base + path, timeout=70)
    except HTTPError as error:
        response = error
    body = response.read().decode()
    assert response.code == expected, (path, response.code, body)
    if count:
        assert response.headers["Content-Type"].startswith("application/json")
        value = json.loads(body)
        assert set(value) == {"ville_ou_pays", "endroits_a_visiter", "prix_moyen_repas"}
        assert len(value["endroits_a_visiter"]) == count
    results.append({"url": base + path, "statut": response.code, "corps": body})
print(json.dumps(results, ensure_ascii=False, indent=2))
