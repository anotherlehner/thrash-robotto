# Dist.sh
# run the release build and create a distributable folder with all resources

# Clear the dist folder if it exists
if [ -d "./dist" ] 
then
    echo "Removing ./dist"
    rm -rf ./dist 
fi

# This section is used for my windows computer -- no need for it now
#if [ -d "/mnt/c/dev/thrash-dist" ]
#then
#    echo "Removing /mnt/c/dev/thrash-dist"
#    rm -rf /mnt/c/dev/thrash-dist
#fi

echo "Making ./dist"
mkdir ./dist

echo "Copying html, js, and css to ./dist"
cp -R ./resources/public/* ./dist/

echo "Building clojurescript"
clojure -m figwheel.main --build-once thrash
#java -cp cljs.jar:src clojure.main release.clj

echo "Copying clojurescript to ./dist"
mkdir ./dist/cljs-out
#cp ./target/thrash-main.js ./dist/cljs-out
cp ./target/public/cljs-out/thrash-main.js ./dist/cljs-out

# Used for windows computer -- ignore for now
#echo "Copying package to windows dev/thrash-dist folder"
#cp -R ./dist /mnt/c/dev/thrash-dist
