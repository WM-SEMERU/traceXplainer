FROM python:3.8

ADD ./requirements.txt .

RUN pip install -r /requirements.txt
RUN apt-get update -y && apt-get install git wget -y


#This needs to be updated. Most likely to 8888 600*
EXPOSE 8050

#Would launch the app on the exposed port
#CMD ["python", "./index.py"]
