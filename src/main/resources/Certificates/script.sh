#!/bin/bash

# Проверка наличия необходимых аргументов
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <csr_file> <key_file> <ca_password> <pkcs12_password> <client_password>"
    exit 1
fi

# Входные файлы
CSR_FILE=$1
KEY_FILE=$2
CA_PASSWORD=$3
PKCS12_PASSWORD=$4
CLIENT_PASSWORD=$5
# Файлы корневого сертификата и ключа
ROOT_CA_CERT="C:/Users/user/IdeaProjects/SpringSecurity-6-Authorization-and-Authentication/src/main/resources/ThridParty/rootCA.crt"
ROOT_CA_KEY="C:/Users/user/IdeaProjects/SpringSecurity-6-Authorization-and-Authentication/src/main/resources/ThridParty/rootCA.key"

# Файлы выходного сертификата и PKCS#12 контейнера
SIGNED_CERT="${CSR_FILE%.csr}.crt"
PKCS12_FILE="${CSR_FILE%.csr}.p12"

# Подписываем сертификат
openssl x509 -req -CA "$ROOT_CA_CERT" -CAkey "$ROOT_CA_KEY" -in "$CSR_FILE" -out "$SIGNED_CERT" -days 365 -CAcreateserial -passin pass:$CA_PASSWORD

# Проверка успешности подписи
if [ $? -ne 0 ]; then
    echo "Failed to sign certificate"
    exit 1
fi

# Создаем PKCS#12 контейнер с использованием передачи ввода через echo и конструкцию | (pipe)
echo -e "$CLIENT_PASSWORD\n$PKCS12_PASSWORD\n$PKCS12_PASSWORD\n" | openssl pkcs12 -export -out "$PKCS12_FILE" -name "${CSR_FILE%.csr}" -inkey "$KEY_FILE" -in "$SIGNED_CERT"

# Проверка успешности создания PKCS#12 контейнера
if [ $? -ne 0 ]; then
    echo "Failed to create PKCS#12 file"
    exit 1
fi

echo "Successfully created signed certificate and PKCS#12 file"