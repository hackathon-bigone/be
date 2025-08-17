#!/bin/bash
set -euo pipefail

APP_DIR=/home/ubuntu/sunsak
JAR_TARGET="/opt/sunsak/app.jar"
SERVICE=sunsak.service
HEALTH_URL=${HEALTH_URL:-http://127.0.0.1:8080/home/foodbox}   # 필요시 변경

echo ">>> Build start"
cd "$APP_DIR"
./gradlew build -x test --console=plain

echo ">>> Pick artifact"
if [[ -f build/libs/app.jar ]]; then
  JAR_SRC=build/libs/app.jar
else
  # plain 제외한 첫 번째 jar 선택
  JAR_SRC=$(ls build/libs/*.jar | grep -v '\-plain\.jar$' | head -n 1 || true)
fi
if [[ -z "${JAR_SRC:-}" ]]; then
  echo "ERROR: JAR not found under build/libs"
  exit 2
fi
echo "Picked: $JAR_SRC"

if ! jar tf "$JAR_SRC" | grep -qi 'BOOT-INF/classes/hackathon/bigone/sunsak/.*controller/'; then
  echo "WARN: controller classes not found in JAR (check packaging)"
fi


echo ">>> Stop service"
sudo systemctl stop "$SERVICE" || true

echo ">>> Install artifact"
sudo install -m 644 "$JAR_SRC" "$JAR_TARGET"

echo ">>> Reload unit"
sudo systemctl daemon-reload

echo ">>> Start service"
sudo systemctl start "$SERVICE"

echo ">>> Wait for healthy"
for i in {1..60}; do
  # 2xx/3xx/4xx는 OK(서버가 응답하면 기동 성공으로 간주), 5xx/실패만 비정상
  CODE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || echo 000)
  if [[ "$CODE" != "000" && "$CODE" -lt 500 ]]; then
    echo "OK: healthy (HTTP $CODE)"
    sudo systemctl status "$SERVICE" --no-pager | sed -n '1,8p'
    exit 0
  fi
  sleep 1
done

echo "ERROR: health check failed (last code: $CODE)"
sudo systemctl status "$SERVICE" --no-pager
journalctl -u "$SERVICE" -n 200 --no-pager
exit 1
