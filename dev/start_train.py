import requests
CONTROL_SECURE_KEY = "123456" #注意修改为application-demo.yml中的secure.key
base_url = "http://localhost:8000/v1/api/data/train"
r = requests.post(base_url, json={'secure_key': CONTROL_SECURE_KEY, 'start_time': 0})
print(r.json())