
from flask import Flask, request

app = Flask(__name__)

@app.route('/')
def hello():
    return 'Hello World!'

@app.route('/route/api/v1.0/anchor', methods=['POST'])
def add_anchor():
    if request.json:
        print request.json

    return 'OK'
