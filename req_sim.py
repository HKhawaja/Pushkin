# -*- coding: utf-8 -*-
import requests, json, datetime,time, base64

#For simulating server requests that android will be making

def createUser(username):
    url = 'http://127.0.0.1:8080/createNewUser'
    data = {
        "username": username,
        "password": 'orlando',
        "email": 'norlando19@amherst.edu',
        "pKey": base64.b64encode('MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIN6+8K3pEdpLTom/0q6vJueof+7UmOaUxFVWkmc2djMfaedV8/x1jnz0Nmy8ZoQECAQegf3Htb0hhmnkCBJ6c/qlS\w8QU1WhjgTfhpBiT8IcIcUtXR2ywpu/CFdUBItp+Gg8Unl7YXTDJT0MHxzd7uu3\+8VXfXWwxDfRMaNcQIDAQAB'),
        "fcmToken": base64.b64encode('cND1HqcOi_k:APA91bFySfUvOwEVEytmFs2mEC0AekTf2Jz_K0nwg-rZQ979v0RKxq8vy5MYBqJZ4VRJnhhrvQOEDPSKlfNc6rgaAmo3fDy3ANU-WOjboE2BhgGX-FsHvqqZbdYwsbPx80wYevdrL8Q_'),
    }

    response = requests.post(url, json=data)
    print response.content

#createUser('aapushkin')

url = 'http://127.0.0.1:8080/login'
data = {
    "username": 'ltolstoy',
    "password": 'orlando',
    "pKey": 'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDIN6+8K3pEdpLTom/0q6vJueof7UmOaUxFVWkmc2djMfaedV8/x1jnz0Nmy8ZoQECAQegf3Htb0hhmnkCBJ6c/qlSyw8QU1WhjgTfhpBiT8IcIcUtXR2ywpu/CFdUBItp+Gg8Unl7YXTDJT0MHxzd7uu3U+8VXfXWwxDfRMaNcQIDAQAB',
    "fcmToken": 'cND1HqcOi_k:APA91bFySfUvOwEVEytmFs2mEC0AekTf2Jz_K0nwg-rZQ979v0RKxq8vy5MYBqJZ4VRJnhhrvQOEDPSKlfNc6rgaAmo3fDy3ANU-WOjboE2BhgGX-FsHvqqZbdYwsbPx80wYevdrL8Q_',
}

response = requests.post(url, json=data)
print response.content
try:
    token = json.loads(response.content.replace('\n', ''))['AuthToken']
except Exception as e:
    print e
    exit()

url = 'http://127.0.0.1:8080/sendMsg'
data = {
    "recipient": "rfrost",
    "message": "YO YO ",#base64.b64encode("QWOHEI"),
    "authorization": '{}:{}'.format("ltolstoy", token) #this will be pulled from a database locally on the app.
}

response = requests.post(url, json=data)
print response.content
