QUALIFIER=$(date +%Y%m%d%H%M)
echo "qualifier: $QUALIFIER"
mvn -DforceContextQualifier=$QUALIFIER \
  clean verify
