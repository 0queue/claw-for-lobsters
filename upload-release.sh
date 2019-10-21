#!/usr/bin/env bash

set -u

API_ENDPOINT="https://api.github.com"
UPLOAD_ENDPOINT="https://uploads.github.com"

TAG=$(git tag)
if [[ ! "$TAG" =~ v[0-9]+ ]]; then
    echo -e "Tag '$TAG' incorrectly formatted"
    exit 1
fi
echo "Tag: $TAG"

# TODO better file name handling
FILE_LOCATION="app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "$FILE_LOCATION" ]]; then
    echo "Please run ./gradlew :app:assembleRelease first"
    exit 2
fi

SIZE_LIMIT=4000000
SIZE=$(stat --printf="%s" "$FILE_LOCATION")
if [[ "$SIZE" -gt "$SIZE_LIMIT" ]]; then
    echo "File too large"
    exit 4
fi


read -p "Github username: " USERNAME
read -s -p "Github password: " PASSWORD
echo

echo "Creating release..."
read -r -d '' CREATE_PAYLOAD <<HERE
{
    "tag_name": "$TAG",
    "name": "$TAG"
}
HERE

echo "$CREATE_PAYLOAD"

RESPONSE=$(curl -s -X POST -u "$USERNAME:$PASSWORD" "$API_ENDPOINT/repos/0queue/claw-for-lobsters/releases" -d "$CREATE_PAYLOAD")

ID=$(echo "$RESPONSE" | jq '.id')
if [[ "$ID" = "null" ]]; then
    echo "Error while creating"
    echo "$RESPONSE"
    echo 3
fi

echo "Created release with ID $ID"

# TODO better file name handling
FILENAME="app-release-$TAG.apk"
echo "Creating release asset $FILENAME..."

RESPONSE2=$(curl -s -X POST -u "$USERNAME:$PASSWORD" "$UPLOAD_ENDPOINT/repos/0queue/claw-for-lobsters/releases/$ID/assets?name=$FILENAME" -H "Content-Type: application/zip" --data-binary "@$FILE_LOCATION")

echo "Done"
