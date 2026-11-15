#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ ! -t 0 ]; then
  echo 'Lance ce script dans un terminal pour saisir la clé de façon masquée.' >&2
  exit 1
fi
printf 'Clé Gemini (saisie masquée) : '
IFS= read -r -s TP3_CLE
printf '\n'
if [ -z "$TP3_CLE" ]; then echo 'La clé est vide.' >&2; exit 1; fi
printf 'Nom du modèle Gemini accessible à ton compte : '
IFS= read -r TP3_MODELE
if [ -z "$TP3_MODELE" ]; then echo 'Le modèle est vide.' >&2; exit 1; fi
umask 077
TP3_CONFIG=$(mktemp .env.XXXXXX)
trap 'rm -f "$TP3_CONFIG"' EXIT
printf 'export GEMINI_API_KEY=%q\nexport GEMINI_MODEL=%q\n' "$TP3_CLE" "$TP3_MODELE" > "$TP3_CONFIG"
mv "$TP3_CONFIG" .env
unset TP3_CLE
printf 'Configuration enregistrée dans .env (privé, exclu de Git). Lance ./lancer.sh.\n'
