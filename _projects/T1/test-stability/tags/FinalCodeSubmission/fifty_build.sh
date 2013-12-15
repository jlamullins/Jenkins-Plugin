echo "fifty_build.sh [job_name] [port num] [no of execution]"
for i in {1..$3}
do
  java -jar jenkins-cli.jar -s http://localhost:$2/ build $1 -s
done

