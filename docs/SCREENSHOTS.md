# SafeGuide — app screenshots

Real captures from the running app on a **Galaxy S25 Ultra**, fully offline. The
calm "SafeGuide" front-end sits over the Lodestar engine — on-device triage,
live compass, and nearest-hospital guidance with no network.

<table>
<tr>
<td align="center"><b>Home</b><br><sub>"How can I help you?" — assistant front and center</sub><br><img src="images/app_02_home.png" width="230"></td>
<td align="center"><b>Assistant</b><br><sub>Voice/text first-aid triage</sub><br><img src="images/app_03_assistant.png" width="230"></td>
<td align="center"><b>My location</b><br><sub>Live compass + approximate position</sub><br><img src="images/app_04_location.png" width="230"></td>
</tr>
<tr>
<td align="center"><b>GPS spoof (demo)</b><br><sub>Detects spoofing, freezes to last trusted fix</sub><br><img src="images/app_05_gps_spoof.png" width="230"></td>
<td align="center"><b>Medical help</b><br><sub>First-aid steps, kit, wound-photo check</sub><br><img src="images/app_06_medical.png" width="230"></td>
<td align="center"><b>Nearby hospital</b><br><sub>Closest care + direction, offline</sub><br><img src="images/app_07_hospital.png" width="230"></td>
</tr>
</table>

### Opening animation

<img src="images/app_01_splash.png" width="230">

---

All screens run with no internet permission. The bottom navigation puts the
assistant in the middle; the **My location** tab includes a demo "Simulate GPS
spoof" control that drives the real `PositionStateMachine` fallback to dead
reckoning, so the spoof-resilience story can be shown live.
