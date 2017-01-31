package al.ahgitdevelopment.municion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;
import java.util.List;

import al.ahgitdevelopment.municion.BillingUtil.IabBroadcastReceiver;
import al.ahgitdevelopment.municion.BillingUtil.IabHelper;
import al.ahgitdevelopment.municion.BillingUtil.IabResult;
import al.ahgitdevelopment.municion.BillingUtil.Inventory;
import al.ahgitdevelopment.municion.DataBases.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.DataModel.Guia;

import static al.ahgitdevelopment.municion.Utils.PREFS_SHOW_ADS;
import static al.ahgitdevelopment.municion.Utils.PURCHASE_ID_REMOVE_ADS;
import static al.ahgitdevelopment.municion.Utils.getStringLicenseFromId;

public class LoginPasswordActivity extends AppCompatActivity implements
        IabBroadcastReceiver.IabBroadcastListener, IabHelper.QueryInventoryFinishedListener {

    public static final int MIN_PASS_LENGTH = 6;
    private final String TAG = "LoginPasswordActivity";
    public Toolbar toolbar;
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    private BaseApplication baseApplication;
    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences prefs;
    private TextInputLayout textInputLayout1;
    private TextInputEditText password1;
    private TextInputLayout textInputLayout2;
    private TextInputEditText password2;
    private ImageView button;
    private TextView versionLabel;
    private AdView mAdView;
    private IabHelper mHelper;
    private boolean isPurchaseAvailable;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        //Gestion de mensajes de firebase en el intent de entrada
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(getLocalClassName(), "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.login);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Inicio de aplicacion");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        //Instances of UI objects
        textInputLayout1 = (TextInputLayout) findViewById(R.id.text_input_layout1);
        password1 = (TextInputEditText) findViewById(R.id.password1);
        textInputLayout2 = (TextInputLayout) findViewById(R.id.text_input_layout2);
        password2 = (TextInputEditText) findViewById(R.id.password2);
        button = (ImageView) findViewById(R.id.continuar);
        versionLabel = (TextView) findViewById(R.id.login_version_label);
        mAdView = (AdView) findViewById(R.id.adView);

        versionLabel.setText(Utils.getAppVersion(this));

        // Registro de contraseña
        if (!prefs.contains("password") || prefs.getString("password", "").equals("")) {
            textInputLayout2.setVisibility(View.VISIBLE);
        } else {
            textInputLayout2.setVisibility(View.GONE);
        }

        //Añadimos la contraseña a las preferencias
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evaluatePassword(prefs);
            }
        });
        password1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:
                        evaluatePassword(prefs);
                        break;
                    case EditorInfo.IME_ACTION_DONE:
                        evaluatePassword(prefs);
                        break;
                    default:
                        Toast.makeText(LoginPasswordActivity.this, "IME erroneo", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String pass = prefs.getString("password", "");
                    if (!pass.equals("") && !password1.getText().toString().equals(pass) && textInputLayout1.getError() != null) {
                        textInputLayout1.setError(getString(R.string.password_equal_fail));
                    }

                    if (password1.getText().toString().equals(pass) && password1.getText().toString().length() >= MIN_PASS_LENGTH)
                        textInputLayout1.setError(null);
                } catch (Exception ex) {
                    Log.e(TAG, "Error en el onTextChange por la version de android");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.toString().equals(password1.getText().toString())) {
                        textInputLayout2.setError(null);
                    } else {
                        textInputLayout2.setError(getString(R.string.password_equal_fail));
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error en el onTextChange por la version de android");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (!prefs.contains(PREFS_SHOW_ADS)) {
            // Agregar la configuración de anuncios en SharedPrefs
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREFS_SHOW_ADS, true);
            editor.apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Comprobación de compra de eliminacion de publicidad
        String base64EncodedPublicKey = getString(R.string.app_public_key);
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.w(TAG, "Problem setting up In-app Billing: " + result.getMessage());
                    isPurchaseAvailable = false;
                } else {
                    isPurchaseAvailable = true;
                    try {
                        mHelper.queryInventoryAsync(LoginPasswordActivity.this /*QueryInventoryFinishedListener*/);
                    } catch (IabHelper.IabAsyncInProgressException ex) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "Error querying inventory. Another async operation in progress.");
                        FirebaseCrash.report(ex);
                    }
                }
            }
        });

        // Important: Dynamically register for broadcast messages about updated purchases.
        // We register the receiver here instead of as a <receiver> in the Manifest
        // because we always call getPurchases() at startup, so therefore we can ignore
        // any broadcasts sent while the app isn't running.
        // Note: registering this listener in an Activity is a bad idea, but is done here
        // because this is a SAMPLE. Regardless, the receiver must be registered after
        // IabHelper is setup, but before first call to getPurchases().
        mBroadcastReceiver = new IabBroadcastReceiver(this /*IabBroadcastListener*/);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(mBroadcastReceiver, broadcastFilter);

        if (prefs.getBoolean(PREFS_SHOW_ADS, true)) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(Utils.getAdRequest(mAdView));
        } else {
            mAdView.setVisibility(View.GONE);
            mAdView.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        // Gestión del año actual para actualizar los cupos y las compras
        Calendar calendar = Calendar.getInstance();
        int yearPref = calendar.get(Calendar.YEAR);

        prefs.edit().putInt("year", yearPref).apply();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        try {
            Log.d(TAG, "Destroying helper.");
            if (mHelper != null) {
                mHelper.disposeWhenFinished();
                mHelper = null;
            }
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Fallo en el dispose de IabHelper");
            FirebaseCrash.report(ex);
        }

        super.onDestroy();
    }

    private void checkAccountPermission() {
        int accountPermission = PackageManager.PERMISSION_GRANTED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accountPermission = checkSelfPermission(Manifest.permission.GET_ACCOUNTS);
            if (accountPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                android.Manifest.permission.GET_ACCOUNTS,
                        },
                        100 //Codigo de respuesta de
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults != null && grantResults.length > 0) {
            if (requestCode == 100) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    checkAccountPermission();
                } else {
                    Log.w(TAG, "Permisos de correo no concedidos");
                }
            }
        }
        //Lanza el tutorial la primera vez
        showTutorial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(LoginPasswordActivity.this, SettingsFragment.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Validación de la contraseña para poder entrar a la aplicación
     *
     * @param prefs Preferencias
     */
    private void evaluatePassword(SharedPreferences prefs) {
        // Registro de usuario
        if (!prefs.contains("password") || prefs.getString("password", "").equals("")) {
            if (savePassword()) { // Guardamos la contraseña
                Toast.makeText(LoginPasswordActivity.this, R.string.password_save, Toast.LENGTH_LONG).show();
                launchActivity();
                finish();
            } else { // Fallo al guardar la contraseñas
                if (textInputLayout1.getError() == null)
                    textInputLayout1.setError(getString(R.string.password_save_fail));
            }
        }
        // Login de usuario
        else {
            if (checkPassword()) { // Password correcta
//                Toast.makeText(LoginPasswordActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
                launchActivity();
                finish();
            } else { // Password incorrecta
                textInputLayout1.setError(getString(R.string.password_fail));
                password1.setText("");
            }
        }
    }

    /**
     * Guarda la contraseña en el sharedPreference
     *
     * @return Contraseña valida o no
     */
    private boolean savePassword() {
        boolean flag = false;

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (password1.getText().toString().length() >= MIN_PASS_LENGTH) {
            if (password1.getText().toString().equals(password2.getText().toString())) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", password1.getText().toString());
                editor.apply();
                flag = true;
            } else {
                textInputLayout2.setError(getString(R.string.password_equal_fail));
            }
        } else {
            flag = false;
            textInputLayout1.setError(getString(R.string.password_short_fail));
        }

        return flag;
    }

    /**
     * Valida la contraseña introducida por el usuario frente a la guardada en el sharedPreferences
     *
     * @return Contraseña valida o invalida
     */
    private boolean checkPassword() {
        boolean isPassCorrect = false;

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String pass = prefs.getString("password", "");
        if (pass.equals(password1.getText().toString())) {
            isPassCorrect = true;
        } else {
            textInputLayout2.setError(getString(R.string.password_equal_fail));
        }

        return isPassCorrect;
    }

    private void launchActivity() {
        DataBaseSQLiteHelper dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());

        //Lanzamiento del Intent
        Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias(null));
        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras(null));
        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias(null));
        intent.putParcelableArrayListExtra("tiradas", dbSqlHelper.getListTiradas(null));

        checkYearCupo(intent);

        startActivity(intent);
        dbSqlHelper.close();

        // Registrar Login - Analytics
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, android_id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    private void checkYearCupo(Intent intent) {
        // Comprobar year para renovar los cupos
        int yearPref = prefs.getInt("year", 0);
        int year = Calendar.getInstance().get(Calendar.YEAR); // Year actual

        if (yearPref != 0 && year > yearPref) {
            List<Guia> listaGuias = intent.getParcelableArrayListExtra("guias");
            if (listaGuias.size() > 0) {
                for (Guia guia : listaGuias) {
                    String nombreLicencia = getStringLicenseFromId(LoginPasswordActivity.this, guia.getTipoLicencia());
                    String idNombreLicencia = nombreLicencia.split(" ")[0];
                    int tipoArma = guia.getTipoArma();

                    switch (idNombreLicencia) {
                        case "A":
                        case "Libro":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 3: // Revolver
                                    guia.setCupo(100);
                                    break;
                                case 1: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 2: // Rifle
                                case 4: // Avancarga
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "B":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 1: // Revolver
                                    guia.setCupo(100);
                                    break;
                            }
                            break;
                        case "C":
                            guia.setCupo(100);
                            break;
                        case "D":
                            guia.setCupo(1000);
                            break;
                        case "E":
                            switch (tipoArma) {
                                case 0: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 1: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "F":
                        case "Federativa":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 3: // Revolver
                                    guia.setCupo(100);
                                    break;
                                case 1: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 2: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "AE":
                            guia.setCupo(1000);
                            break;
                        case "AER":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 2: // Revolver
                                    guia.setCupo(100);
                                case 1: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Lanza el tutorial la primera vez que se inicia la aplicación.
     */
    private void showTutorial() {
        // Para que no se muestre el tutorial cuando se ha reseteado el password
        boolean isTutorial = true;
        if (getIntent().hasExtra("tutorial"))
            isTutorial = getIntent().getBooleanExtra("tutorial", true);

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (prefs.getBoolean("show_tutorial", true) && isTutorial) {
            Intent intent = new Intent(this, FragmentTutorialActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        try {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.e(TAG, "Error obteniendo los detalles de los productos" + result.getMessage());
                return;
            }

            //Si el usuario ha comprado la eliminación de anuncios
            if (inventory.hasPurchase(PURCHASE_ID_REMOVE_ADS)) {
                //pero no tiene actualizado su shared prefs
                if (prefs.getBoolean(PREFS_SHOW_ADS, true)) {
                    // Eliminamos la publicidad
                    mAdView.setVisibility(View.GONE);
                    mAdView.setEnabled(false);

                    // Actualizamos las preferencias
                    prefs.edit().putBoolean(PREFS_SHOW_ADS, false).apply();
                }
            } else {
                prefs.edit().putBoolean(PREFS_SHOW_ADS, true).apply();
                mAdView.setVisibility(View.VISIBLE);
                mAdView.setEnabled(true);
                mAdView.loadAd(Utils.getAdRequest(mAdView));
            }

            checkAccountPermission();

        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error en el proceso de onQueryInventoryFinished");
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(LoginPasswordActivity.this /*QueryInventoryFinishedListener*/);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            Log.e(TAG, "Error querying inventory. Another async operation in progress.", ex);
        }
    }
}


//https://github.com/firebase/quickstart-android/blob/master/crash/app/src/main/java/com/google/samples/quickstart/crash/MainActivity.java
//        FirebaseCrash.logcat(Log.INFO, "LoginPasswordActivity", "Crash DONE");
//        if(catchCrashCheckBox.isChecked()){
//            try{
//                throw new NullPointerException();
//            }catch(NullPointerException ex){
//                // [START log_and_report]
//                FirebaseCrash.logcat(Log.ERROR,TAG,"NPE caught");
//                FirebaseCrash.report(ex);
//                // [END log_and_report]
//            }
//        }else{
//            throw new NullPointerException();
//        }