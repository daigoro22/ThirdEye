# ThirdEye<br>
## Overview
同じWifiネットワーク上の2台のAndroid端末間で、映像の送受信を行います。<br>
受信側は、通常表示の他に、受け取った映像をVR表示することができます。<br>
VR表示の際には、端末に対応したVRゴーグルが必要になります。<br>

## Description
VR表示には、CardBoard SDKを用いました。　<br>
OpenGLの座標系に半球を描画し、その内側に受信した映像をテクスチャに変換して貼り付けています。<br>
映像を送信する端末に魚眼レンズを取り付けることで、端末のある場所に自分がいるような感覚を味わうことができます。<br><br>
映像の送受信にはJava標準のUDPライブラリを用いました。<br>
映像をパケットの最大サイズごとに分割し、最初のバイトに番号をつけることで、パケットの順番が入れ替わっても修正することができます。<br><br>
おまけ機能として、VR表示の際、受信側を装着した人の頭の動きに合わせて、イヤホンジャックの端子から　、サーボモータを制御することができます。<br>
端末ごとに端子から出力できる最大電圧が異なるため、端末によってはうまく動作しない場合があります。<br>
動作確認済端末: Nexus6 zenfone2

## Results 
高専ロボコン2017 関東甲信越大会において、人型ロボットとの視点共有を行い、「人機一体」の操縦を行いました。<br>
ロボットの頭の部分に装着した端末から魚眼レンズを通した映像を送り、これを操縦者がヘッドマウントディスプレイとして装着した端末で受け取ることにより、ロボットとの視点共有を実現しました。<br>
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
