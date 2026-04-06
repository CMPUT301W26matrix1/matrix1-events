# 🎟️ Event Flow

<p align="center">
  <img src="https://github.com/user-attachments/assets/4aff5645-e876-4060-b4ce-fe4f8a9404ea" 
       alt="App Design Animation" 
       width="100%"/>
</p>

full demo video link - https://drive.google.com/drive/folders/19VsZlUG890pAXhSYkqBpkPsShjqNSp2s?usp=sharing
## Description

This is the repository for the group project for team `matrix1` in CMPUT 301 at the University of Alberta.

The project, an Android app, is an event lottery system that allows users to join a waitlist for events and be selected by lottery to join. Organizers can create and manage events for others to sign up.


Key-Features
Smart Pooling System: Automatically manage event interest by pulling selected entrants from a live waiting list. Organizers can run lotteries or manually choose participants with ease.

QR Code Event Access: Entrants can instantly access event pages by scanning a QR code — view details, join waiting lists, and stay connected without manual searching.

Real-Time Firebase Sync: All event data, attendee updates, notifications, and check-ins stay synced across devices in real time through Firebase integration.

Role-Based User Experience: Entrants, Organizers, and Admins each get a tailored interface and permissions designed specifically for their role within the app.

Event Poster Uploads: Organizers can upload custom event posters to make their event pages more engaging and visually distinct.

Optional Geolocation Verification: When enabled by organizers, entrants’ join locations are captured from the device to verify authenticity and support location-based event rules.

Dedicated Organizer Map Console: A specialized map interface available exclusively to event creators. Organizers can set, validate, and manage precise event coordinates using an integrated place-picker, ensuring accurate location data for geolocation rules and event logistics.

Visit the Project 
[Wiki](https://github.com/CMPUT301W26matrix1/matrix1-events/wiki) and for
[Javadocs](https://github.com/CMPUT301W26matrix1/matrix1-events/tree/main/code/javadoc), open the `code` folder in the terminal and run:
```bash
open javadoc/index.html
```



## Technologies

- Android
- Firebase

## Getting Started

### Prerequisites

- Android Studio (latest version)
- Java 17+

### Setup

1. Clone the repository

```bash
git clone https://github.com/CMPUT301W26matrix1/matrix1-events.git
```

2. Open the `code` folder in Android Studio

3. Setup the `google-services.json` file

- Download or receive the `google-services.json` in order to enable Firebase
- Place it at the directory `code/app/google-services.json`

4. Setup the Google Maps for Android SDK API Key

- Create the a file in `code` called `secrets.properties` which contains the API key as follows:
```bash
MAPS_API_KEY=<API KEY HERE>
```
