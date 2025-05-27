import firebase_admin
from firebase_admin import credentials, messaging, firestore
from datetime import datetime
import random

# 1. Firebase 초기화
cred = credentials.Certificate("/Users/junhyuk/AndroidStudioProjects/TripCast/credentials.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# 무작위 날씨 생성
def get_real_weather(city, date):
    conditions = ["Sunny", "Cloudy", "Rainy", "Snowy"]
    return random.choice(conditions)

# 2. 오늘 날짜
today = datetime.now().date().isoformat()

# 3. Firestore에서 여행 계획 불러오기
docs = db.collection("plans").stream()

# 4. FCM v1 방식으로 알림 전송
def send_fcm_v1(token, title, body):
    message = messaging.Message(
        notification=messaging.Notification(title=title, body=body),
        token=token
    )
    response = messaging.send(message)
    print(f"📬 v1 알림 전송 완료: {response}")

# 5. 여행 일정 날씨 비교 및 알림
for doc in docs:
    data = doc.to_dict()
    print(f"📦 여행 문서 확인: {data}")

    token = "d9HFDFKWRcWmgz8x7KrxrJ:APA91bEJ_lp6_5Eec95xx9kQGm1AredF20vXScSG8StvFbs4bWi12OlDkKFiZB3Fltd42oPFUASuPMXND5DNunGcUQjzO6qrkmNs6zj5Rnq5SRP4__nQk_s"  # 예: d9HFDF...
    destination = data["destination"]
    weather_list = data.get("weather", [])

    for w in weather_list:
        target_date = w["date"]
        expected = w["condition"]
        actual = get_real_weather(destination, target_date)

        if expected.lower() != actual.lower():
            print(f"❗차이 발생: {destination} | {target_date} | 예상: {expected}, 실제: {actual}")
            send_fcm_v1(token, f"{target_date} 날씨 변경", f"{destination}의 날씨가 예상과 달라요!")
        else:
            print(f"✅일치: {destination} | {target_date} | {expected}")