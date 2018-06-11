# ThirdEye
同じWifiネットワーク上の2台のAndroid端末間で、映像の送受信を行います。<br>
受信側は、通常表示の他に、受け取った映像をVR表示することができます。<br>
VR表示には、CardBoard SDKを用いました。　<br>
OpenGLの座標系に半球を描画し、その内側に受信した映像をテクスチャに変換して貼り付けています。<br>
映像を送信する端末に魚眼レンズを取り付けることで、端末のある場所に自分がいるような感覚を味わうことができます。<br>

## Results 
高専ロボコン2017において、人型ロボットとの視点共有を行い、人機一体の操縦を行いました。<br>
ロボットの頭の部分に装着した端末から魚眼レンズを通した映像を送り、これを操縦者がヘッドマウントディスプレイとして装着した端末で受け取ることにより、ロボットとの視点共有を実現しています。<br>
<br>

## Demo
ロボット視点の映像<br>
https://youtu.be/5-V3vkgjPl8?list=LLGs9sKnfl4wub6_mCbmzC0w
<br>

## Requirement
OpenCV 3.1.0 Android SDK<br>
https://sourceforge.net/projects/opencvlibrary/files/opencv-android/

Google CardBoard SDK<br>
https://developers.google.com/vr/develop/android/get-started
