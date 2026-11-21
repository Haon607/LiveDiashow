# ==== CONFIG ====
$remoteUser = "rb"
$remoteHost = "192.168.0.6"
$remoteDir = "ld"
$remote = "$remoteUser@$remoteHost"
$tempDeploy = "tempDeploy"

# ==== Build Spring ====
Write-Host "Building Spring Boot app..."
Push-Location LiveDiashowBackend
./gradlew clean build -x test
Pop-Location

# ==== Build Angular ====
Write-Host "Building Angular app..."
Push-Location LiveDiashowFrontend
npm install
npm run build --configuration=production
Pop-Location

# ==== Prepare Deployment Folder ====
Write-Host "Preparing deployment package..."
Remove-Item -Recurse -Force $tempDeploy -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "$tempDeploy/LiveDiashowBackend"
New-Item -ItemType Directory -Force -Path "$tempDeploy/LiveDiashowFrontend"

Copy-Item "LiveDiashowBackend/build/libs/*.jar" -Destination "$tempDeploy\LiveDiashowBackend\"
Copy-Item "LiveDiashowBackend/Dockerfile" -Destination "$tempDeploy\LiveDiashowBackend\"

Copy-Item -Recurse -Path "LiveDiashowFrontend\dist" -Destination "$tempDeploy\LiveDiashowFrontend\dist"
Copy-Item -Path "LiveDiashowFrontend\Dockerfile" -Destination "$tempDeploy\LiveDiashowFrontend\"
Copy-Item -Path "LiveDiashowFrontend\nginx.conf" -Destination "$tempDeploy\LiveDiashowFrontend\"

Copy-Item "docker-compose.yml" $tempDeploy

# ==== Ensure Remote Dir Exists ====
ssh $remote "mkdir -p $remoteDir"

# ==== Clean Remote ====
Write-Host "Cleaning remote Raspberry Pi..."
ssh $remote "mkdir -p $remoteDir && cd $remoteDir && docker compose down && rm -rf ./*"

# ==== Upload Files ====
Write-Host "Uploading files to Raspberry Pi..."
scp -r "$tempDeploy/*" "${remote}:$remoteDir"

# ==== Launch Docker ====
Write-Host "Starting containers on Raspberry Pi..."
ssh $remote "cd $remoteDir && docker compose up -d --build && docker exec ld-angular-1 chmod -R 755 /usr/share/nginx/html && docker exec ld-angular-1 chown -R nginx:nginx /usr/share/nginx/html"

# ==== Cleanup ====
Write-Host "Cleaning local temp files..."
Remove-Item -Recurse -Force $tempDeploy

Write-Host "Done! App should be running on http://$remoteHost"