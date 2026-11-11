@echo off
echo Mandando para o GitHub...
git add .
git commit -m "Atualizacao automatica de rotina"
git push
echo.
echo Tudo salvo na nuvem! Pode fechar e ir dormir.
timeout /t 5