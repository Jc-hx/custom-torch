import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:settings_ui/settings_ui.dart';

const platform = MethodChannel('torch_control_channel');
const platform2 = MethodChannel('tile_channel');

Color mainDark = Colors.black;
Color mainLight = Colors.white;
Color mainDark2 = Colors.white;
Color mainLight2 = Colors.black;
int torchStatus = 0;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await requestCameraPermission();
  getColor1();
  getColor2();
  getColor3();
  getColor4();
  getStatus();
  runApp(const MyApp());
}

Future<void> requestCameraPermission() async {
  await Permission.camera.request();
  var status = await Permission.camera.status;
  if (await Permission.speech.isPermanentlyDenied) {
    openAppSettings(); //Open settings if user won't allow permission
  }
  if (status.isDenied) {
    exit(0); //Exit the app if permission is denied
  }
}

Future<void> getColor1() async {
  int color = await _getColor1();
  //print("color : $color");
  mainDark = Color(color);
  //print("color 1: $mainDark");
}

Future<void> getColor2() async {
  int color = await _getColor2();
  //print("color : $color");
  mainLight = Color(color);
  //print("color 2: $mainLight");
}

Future<void> getColor3() async {
  int color = await _getColor3();
  //print("color : $color");
  mainDark2 = Color(color);
}

Future<void> getColor4() async {
  int color = await _getColor4();
  //print("color : $color");
  mainLight2 = Color(color);
}

Future<void> getStatus() async {
  int status = await _getStatus();
  //print("status : status");
  torchStatus = status;
}

Future<int> _getColor1() async {
  try {
    final int result = await platform.invokeMethod('getColor1');
    return result;
  } on PlatformException catch (e) {
    print("Failed to get color: '${e.message}'.");
    return 0; // Return a default color or handle the error accordingly
  }
}

Future<int> _getColor2() async {
  try {
    final int result = await platform.invokeMethod('getColor2');
    return result;
  } on PlatformException catch (e) {
    print("Failed to get color: '${e.message}'.");
    return 0; // Return a default color or handle the error accordingly
  }
}

Future<int> _getColor3() async {
  try {
    final int result = await platform.invokeMethod('getColor3');
    return result;
  } on PlatformException catch (e) {
    print("Failed to get color: '${e.message}'.");
    return 0; // Return a default color or handle the error accordingly
  }
}

Future<int> _getColor4() async {
  try {
    final int result = await platform.invokeMethod('getColor4');
    return result;
  } on PlatformException catch (e) {
    print("Failed to get color: '${e.message}'.");
    return 0; // Return a default color or handle the error accordingly
  }
}

Future<int> _getStatus() async {
  try {
    final int result = await platform.invokeMethod('checkStatus');
    return result;
  } on PlatformException catch (e) {
    print("Failed to get torch status: '${e.message}'.");
    return 0;
  }
}

Future<void> _checkPrefs() async {
  try {
    await platform.invokeMethod('checkPrefs');
  } on PlatformException catch (e) {
    print("Failed to check shared prefs: '${e.message}'.");
  }
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Custom Torch',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          primary: mainLight2,
          background: Colors.black87,
          seedColor: mainDark2,
          outline: mainLight2,
          onPrimary: mainDark2,
          surfaceVariant: mainDark2,
          brightness: Brightness.light,
        ),
      ),

      darkTheme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          primary: mainLight,
          background: Colors.black87,
          seedColor: mainDark,
          outline: mainLight,
          onPrimary: mainDark,
          surfaceVariant: mainDark,
          brightness: Brightness.dark,
        ),

        // Your dark theme configurations here
      ),
      themeMode: ThemeMode.system,
      home: const MyHomePage(title: 'Custom Torch Home Page'),
    );

  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('torch_control_channel');
  static const platform2 = MethodChannel('tile_channel');

  bool isTorchOn = false;

  int brightnessLevel = 1; // Initial brightness level
  int maxBrightness = 45;
  int stepsNumber = 5; // Initial steps number
  bool vibrationsMenu = true;
  bool vibrationsTile = true;
  bool vibrationsPopup = true;
  bool tileEffect = true;
  bool popupAutoOn = true;
  bool popupAutoOff = true;

  @override
  void initState() {
    super.initState();

    _getPrefs();
    _getFlash();

    if (torchStatus == 0) {
      isTorchOn = false;
    } else {
      isTorchOn = true;
    }

    platform2.setMethodCallHandler

    (

    (call) async {
    if (call.method == "onTileClick") {
    print("Tile Clicked!");
    _toggleTorch();
    } else if (call.method == "onTileAdded") {
    print("Tile Added !");
    Fluttertoast.showToast(
    msg: "Tile added.",
    toastLength: Toast.LENGTH_SHORT,
    gravity: ToastGravity.CENTER,
    timeInSecForIosWeb: 1,
    backgroundColor: Colors.black,
    textColor: Colors.white,
    fontSize: 16.0
    );
    } else if (call.method == "onTileAlreadyAdded") {
    print("Tile Already Added !");
    Fluttertoast.showToast(
    msg: "Tile already added.",
    toastLength: Toast.LENGTH_SHORT,
    gravity: ToastGravity.CENTER,
    timeInSecForIosWeb: 1,
    backgroundColor: Colors.black,
    textColor: Colors.white,
    fontSize: 16.0
    );
    } else if (call.method == "onToggle") {
      print("Toggled from tile !");
      setState(() {
        isTorchOn = !isTorchOn;
      });
    }
    });
  }

  void _toggleTorch() {
    //HapticFeedback.selectionClick();
    setState(() {
      isTorchOn = !isTorchOn;
      if (isTorchOn) {
        _turnOnTorch(brightnessLevel);
        _toggleOn();
      } else {
        _turnOffTorch();
        _toggleOff();
      }
    });
  }

  // void _increaseBrightness() {
  //   HapticFeedback.selectionClick();
  //   setState(() {
  //     if (brightnessLevel < stepsNumber) {
  //       brightnessLevel += 1;
  //       _savePrefs(brightnessLevel);
  //       _savePrefsAndroid(brightnessLevel);
  //       if (isTorchOn) {
  //         _turnOnTorch(brightnessLevel);
  //       }
  //     } else {
  //       HapticFeedback.vibrate();
  //     }
  //   });
  // }
  //
  // void _decreaseBrightness() {
  //   HapticFeedback.selectionClick();
  //   setState(() {
  //     if (brightnessLevel > 1) {
  //       brightnessLevel -= 1;
  //       _savePrefs(brightnessLevel);
  //       _savePrefsAndroid(brightnessLevel);
  //       if (isTorchOn) {
  //         _turnOnTorch(brightnessLevel);
  //       }
  //     } else {
  //       HapticFeedback.vibrate();
  //     }
  //   });
  // }

  final Uri _url = Uri.parse('https://github.com/Jc-hx/Custom-Torch');

  Future<void> _launchUrl() async {
    if (!await launchUrl(_url)) {
      throw Exception('Could not launch $_url');
    }
  }

  Future<void> _popup() async {
    try {
      await platform.invokeMethod('popup');
    } on PlatformException catch (e) {
      print("Failed to pop up: ${e.message}");
    }
  }

  Future<void> _turnOnTorch(int brightness) async {
    brightness = brightness * maxBrightness ~/ stepsNumber;
    if (brightness == 0 || brightness <= maxBrightness ~/ stepsNumber) {
      brightness = 1;
    }
    try {
      await platform.invokeMethod('turnOnTorchWithStrengthLevel', brightness);
    } on PlatformException catch (e) {
      print("Failed to turn on torch: ${e.message}");
    }
  }

  Future<void> _savePrefs(int brightnessLevel) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setInt('brightnessLevel', brightnessLevel);
  }

  Future<void> _savePrefs2(int stepsNumber) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setInt('stepsNumber', stepsNumber);
  }

  Future<void> _getPrefs() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    int? brightnessLevelLocal = prefs.getInt('brightnessLevel');
    int? stepsNumberLocal = prefs.getInt('stepsNumber');
    bool? vibrationsMenuLocal = prefs.getBool('vibrationsMenu');
    bool? vibrationsTileLocal = prefs.getBool('vibrationsTile');
    bool? vibrationsPopupLocal = prefs.getBool('vibrationsPopup');
    bool? tileEffectLocal = prefs.getBool('tileEffect');
    bool? popupAutoOnLocal = prefs.getBool('popupAutoOn');
    bool? popupAutoOffLocal = prefs.getBool('popupAutoOff');

    setState(() {
      brightnessLevel = brightnessLevelLocal ?? 1;
      stepsNumber = stepsNumberLocal ?? 5;
      vibrationsMenu = vibrationsMenuLocal ?? true;
      vibrationsTile = vibrationsTileLocal ?? true;
      vibrationsPopup = vibrationsPopupLocal ?? true;
      tileEffect = tileEffectLocal ?? true;
      popupAutoOn = popupAutoOnLocal ?? true;
      popupAutoOff = popupAutoOffLocal ?? true;

    });
  }

  Future<void> _getFlash() async {
    try {
      maxBrightness = await platform.invokeMethod('checkFlash');
      print("maxBrightness : $maxBrightness");
    } on PlatformException catch (e) {
      print("Failed to check torchlight compatibility: ${e.message}");
    }
  }

  Future<void> _turnOffTorch() async {
    try {
      await platform.invokeMethod('turnOffTorch');
    } on PlatformException catch (e) {
      print("Failed to turn off torch: ${e.message}");
    }
  }

  Future<void> _addTileToQuickSettings() async {
    if (vibrationsMenu) {
      HapticFeedback.selectionClick();
    }
    try {
      await platform2.invokeMethod('addTile');
    } on PlatformException catch (e) {
      print("Failed to add tile: '${e.message}'.");
    }
  }

  Future<void> _toggleOn() async {
    try {
      await platform2.invokeMethod('toggleOn');
    } on PlatformException catch (e) {
      print("Failed to toggle on tile: '${e.message}'.");
    }
  }

  Future<void> _toggleOff() async {
    try {
      await platform2.invokeMethod('toggleOff');
    } on PlatformException catch (e) {
      print("Failed to toggle off tile: '${e.message}'.");
    }
  }

  Future<void> _savePrefsAndroid(int brightnessLevel) async {
    try {
      await platform.invokeMethod('saveBrightness', brightnessLevel);
    } on PlatformException catch (e) {
      print("Failed to save torch brightness: '${e.message}'.");
    }
  }

  Future<void> _savePrefs2Android(int stepsNumber) async {
    try {
      await platform.invokeMethod('saveStepsNumber', stepsNumber);
    } on PlatformException catch (e) {
      print("Failed to save steps number: '${e.message}'.");
    }
  }

  Future<void> _savePrefsU(String data, bool value) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    prefs.setBool(data, value);
  }

  Future<void> _savePrefsAndroidU(String data, bool value) async {
    try {
      await platform.invokeMethod(data, value);
    } on PlatformException catch (e) {
      print("Failed to save bool value '${e.message}'.");
    }
  }

  List<int> stepsList = [3, 5, 10];

    @override
  Widget build(BuildContext context) {
    const kFontFam = 'MyFlutterApp';
    const String? kFontPkg = null;
    const IconData githubCircled = IconData(0xf09b, fontFamily: kFontFam, fontPackage: kFontPkg);
    getColor1();
    getColor2();
    getColor3();
    getColor4();
    if (Theme.of(context).brightness == Brightness.dark) {
      //do nothing
    } else {
      mainDark = mainDark2;
      mainLight = mainLight2;
    }

    return Scaffold(
      backgroundColor: mainDark,
      appBar: AppBar(
        title: Text('Custom Torch',
          style: GoogleFonts.lato(
            color: Theme.of(context).colorScheme.primary,
            fontSize: 22,
            fontWeight: FontWeight.w300,
          ),
        ),
        actions: [
          IconButton(
            iconSize: 30,
            color: Theme.of(context).colorScheme.primary,
            icon: const Icon(
              githubCircled,
            ),
            onPressed: () {
              _launchUrl();
            },
          ),
        ],
        centerTitle: false,
        backgroundColor: mainDark,
        elevation: 0,
      ),
      body: SettingsList(
        lightTheme: SettingsThemeData(
          dividerColor: Colors.white,
          tileDescriptionTextColor: mainLight,
          leadingIconsColor: mainLight,
          settingsListBackground: mainDark,
          settingsSectionBackground: mainDark,
          settingsTileTextColor: Colors.black87,
          tileHighlightColor: mainLight,
          titleTextColor: Theme.of(context).colorScheme.primary,
          trailingTextColor: Colors.orange,
        ),
        darkTheme: SettingsThemeData(
          dividerColor: Colors.white,
          tileDescriptionTextColor: mainLight,
          leadingIconsColor: mainLight,
          settingsListBackground: mainDark,
          settingsSectionBackground: mainDark,
          settingsTileTextColor: Colors.white,
          tileHighlightColor: mainLight,
          titleTextColor: Theme.of(context).colorScheme.primary,
          trailingTextColor: Colors.orange,
        ),
        sections: [
          SettingsSection(
            title: const Text('Tile Settings',
                style: TextStyle(fontWeight: FontWeight.bold)
            ),
            tiles: <CustomSettingsTile>[
              CustomSettingsTile(
                child: Row(
                  children: <Widget>[
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.only(left: 16),
                        child: Slider(
                          value: brightnessLevel.toDouble(), // The current value of the slider
                          onChanged: (newValue) {
                            setState(() {
                              brightnessLevel = newValue.toInt();
                              if (!isTorchOn && brightnessLevel != 1 && brightnessLevel != stepsNumber) {
                                //HapticFeedback.lightImpact();
                              }
                              if (brightnessLevel == 0) {
                                brightnessLevel = 1;
                              }
                              _savePrefs(brightnessLevel);
                              _savePrefsAndroid(brightnessLevel);
                              if (isTorchOn) {
                                _turnOnTorch(brightnessLevel);
                              }
                              if (brightnessLevel == 1 || brightnessLevel == stepsNumber) {
                                if (vibrationsMenu) {
                                  HapticFeedback.heavyImpact();
                                }
                              }
                            });
                          },
                          min: 1, // Minimum value
                          max: stepsNumber.toDouble(), // Maximum value
                          divisions: stepsNumber - 1, // Number of divisions between min and max
                          label: brightnessLevel.toString(),
                          inactiveColor: Theme.of(context).colorScheme.secondary,
                        ),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Padding(
                      padding: const EdgeInsets.only(right: 14),
                      child: GestureDetector(
                        onLongPress: () {
                        _popup();
                        if (vibrationsMenu) {
                          HapticFeedback.selectionClick();
                        }
                        print('Button long pressed!');
                        },
                        child: Material(
                          color: Colors.transparent,
                          child: Ink(
                            decoration: ShapeDecoration(
                              color: isTorchOn ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.surfaceVariant,
                              shape: CircleBorder(
                                  side: BorderSide(color: isTorchOn ? Theme.of(context).colorScheme.primary :
                                  Theme.of(context).colorScheme.outline, width: 2.0)),
                            ),
                            child: IconButton(
                              icon: Icon(
                                  !isTorchOn ? Icons.flashlight_on_rounded : Icons.flashlight_off_rounded,
                                  color: isTorchOn ? Theme.of(context).colorScheme.onPrimary : mainLight), // Button icon
                              onPressed: () {
                                _toggleTorch();

                                setState(() {

                                });
                                print('Button pressed!');
                              },
                            ),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              CustomSettingsTile(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    const Text(
                      'Steps number : ', // Tile title
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(width: 16),
                    DropdownButton<int>(
                      value: stepsNumber,
                      onChanged: (int? newValue) {
                        setState(() {
                          brightnessLevel = brightnessLevel * newValue! ~/ stepsNumber;
                          if (brightnessLevel == 0) {
                            brightnessLevel = 1;
                          }
                          stepsNumber = newValue;
                          _savePrefs(brightnessLevel);
                          _savePrefsAndroid(brightnessLevel);
                          _savePrefs2(stepsNumber);
                          _savePrefs2Android(stepsNumber);
                          if (vibrationsMenu) {
                            HapticFeedback.selectionClick();
                          }
                        });
                      },
                      items: stepsList.map<DropdownMenuItem<int>>((int value) {
                        return DropdownMenuItem<int>(
                          value: value,
                          child: Text(value.toString()), // Display item in the dropdown
                        );
                      }).toList(),
                      // Replace `yourList` with your actual list of integers
                    ),
                  ],
                ),
              ),

              CustomSettingsTile(
                child: Column(
                  children: <Widget>[
                    const SizedBox(height: 15),
                    ElevatedButton(
                      onPressed: () {
                        _addTileToQuickSettings();
                        print('Button tapped!');
                      },
                      style: ElevatedButton.styleFrom(
                        foregroundColor: Colors.white,
                        backgroundColor: mainDark,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8), // Optional: Set border radius for rounded corners
                          side: BorderSide(color: Theme.of(context).colorScheme.outline, width: 2), // Border color and width
                        ),
                      ),
                      child: Text(
                        'Add tile to Quick Settings',
                          style: TextStyle(color: Theme.of(context).colorScheme.primary)
                      ),
                    ),
                  ],
                ),
              )
            ],
          ),
          SettingsSection(
            title: const Text('Vibrations',
                style: TextStyle(fontWeight: FontWeight.bold)
            ),
            tiles: <SettingsTile>[
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    vibrationsMenu = value;
                    _savePrefsU('vibrationsMenu', value);
                    _savePrefsAndroidU('vibrationsMenu', value);
                    _checkPrefs();
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: vibrationsMenu,
                leading: const Icon(Icons.menu_rounded),
                title: const Text('Menu'),
                description: const Text('Switch on/off this page vibrations feedback.'),
              ),
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    vibrationsTile = value;
                    _savePrefsU('vibrationsTile', value);
                    _savePrefsAndroidU('vibrationsTile', value);
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: vibrationsTile,
                leading: const Icon(Icons.flash_on_rounded),
                title: const Text('Tile'),
                description: const Text('Switch on/off quick settings tile vibrations feedback.'),
              ),
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    vibrationsPopup = value;
                    _savePrefsU('vibrationsPopup', value);
                    _savePrefsAndroidU('vibrationsPopup', value);
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: vibrationsPopup,
                leading: const Icon(Icons.add_to_home_screen_rounded),
                title: const Text('Popup UI'),
                description: const Text('Switch on/off popup vibrations feedback.'),
              ),
            ],
          ),
          SettingsSection(
            title: const Text('Miscellaneous',
              style: TextStyle(fontWeight: FontWeight.bold)
            ),
            tiles: <SettingsTile>[
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    tileEffect = value;
                    _savePrefsU('tileEffect', value);
                    _savePrefsAndroidU('tileEffect', value);
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: tileEffect,
                leading: const Icon(Icons.animation_rounded),
                title: const Text('Tile Effect'),
                description: const Text('Progressive switch on/off.'),
              ),
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    popupAutoOn = value;
                    _savePrefsU('popupAutoOn', value);
                    _savePrefsAndroidU('popupAutoOn', value);
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: popupAutoOn,
                leading: const Icon(Icons.auto_awesome_rounded),
                title: const Text('Popup Auto Switch On'),
                description: const Text('Auto switch on when bringing popup.'),
              ),
              SettingsTile.switchTile(
                onToggle: (value) {
                  setState(() {
                    popupAutoOff = value;
                    _savePrefsU('popupAutoOff', value);
                    _savePrefsAndroidU('popupAutoOff', value);
                    if (vibrationsMenu) {
                      HapticFeedback.selectionClick();
                    }
                  });
                },
                initialValue: popupAutoOff,
                leading: const Icon(Icons.auto_awesome_outlined),
                title: const Text('Popup Auto Switch Off'),
                description: const Text('Auto switch off when dismissing popup.'),
              ),
              SettingsTile.navigation(
                onPressed: (context) {
                  showDialog(
                    context: context,
                    builder: (BuildContext context) {
                      return AlertDialog(
                        backgroundColor: mainDark,
                        shape: const RoundedRectangleBorder(
                          borderRadius: BorderRadius.all(Radius.circular(10.0)),
                          //side: BorderSide(color: Colors.red),
                        ),
                        content: SizedBox(
                          height: 300.0,
                          width: 300.0,
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text('This app creates a tile in the quick settings so you can customize the brightness and access a slider over your apps.',
                                style: GoogleFonts.lato(
                                  color: mainLight,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                              const SizedBox(height: 20),
                              Text('Has been made with love in October 2023.',
                                style: GoogleFonts.lato(
                                  color: mainLight,
                                  fontWeight: FontWeight.w100,
                                ),
                              ),
                            ],
                          ),
                        ),
                        actions: [
                          TextButton(onPressed: () {
                            Navigator.pop(context);
                            if (vibrationsMenu) {
                              HapticFeedback.selectionClick();
                            }
                          },
                            child: Text('OK',
                              style: GoogleFonts.lato(
                                color: mainLight,
                              ),
                            ),
                          ),
                        ],
                      );
                    },
                  );
                },
                leading: const Icon(Icons.info_outline),
                title: const Text('About'),
                //inactiveTrackColor: Colors.green,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
