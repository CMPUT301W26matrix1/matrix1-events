# 🎟️ Event Flow

<p align="center">
  <video src="https://github.com/user-attachments/assets/05247ed0-b100-4eff-b377-3bfa01a641fb" width="700px" controls autoplay loop muted>
</p>
    
## Description

This is the repository for the group project for team `matrix1` in CMPUT 301 at the University of Alberta.

The project, an Android app, is an event lottery system that allows users to join a waitlist for events and be selected by lottery to join. Organizers can create and manage events for others to sign up.

See the project [Wiki](https://github.com/CMPUT301W26matrix1/matrix1-events/wiki) for more information.

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
