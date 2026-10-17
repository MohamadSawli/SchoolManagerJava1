package com.example.schoolmanager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private AppDatabase database;
    private LinearLayout studentsContainer;
    private TextView countText;
    private EditText searchInput;
    private String currentQuery = "";
    private List<Student> currentStudents = new ArrayList<>();

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = AppDatabase.getInstance(this);
        buildScreen();
        loadStudents("");
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(0xFFF5F7FB);
        root.setPadding(dp(16), dp(18), dp(16), dp(12));

        TextView title = new TextView(this);
        title.setText("إدارة المدرسة والمعهد");
        title.setTextSize(25);
        title.setTextColor(0xFF15345B);
        title.setGravity(Gravity.RIGHT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("إدارة بيانات الطلاب وحفظها على الجهاز");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF65758B);
        subtitle.setPadding(0, dp(5), 0, dp(14));
        root.addView(subtitle, matchWrap());

        LinearLayout summary = new LinearLayout(this);
        summary.setOrientation(LinearLayout.HORIZONTAL);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        summary.setBackgroundColor(0xFFFFFFFF);
        summary.setPadding(dp(14), dp(12), dp(14), dp(12));
        countText = new TextView(this);
        countText.setText("عدد الطلاب: 0");
        countText.setTextSize(16);
        countText.setTextColor(0xFF15345B);
        countText.setTypeface(null, android.graphics.Typeface.BOLD);
        summary.addView(countText, new LinearLayout.LayoutParams(0, -2, 1));
        Button addButton = makeButton("+ إضافة طالب");
        addButton.setOnClickListener(v -> showStudentDialog(null));
        summary.addView(addButton, wrapWrap());
        root.addView(summary, matchWrap());

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setTextSize(15);
        searchInput.setHint("ابحث بالاسم أو الرقم أو الصف...");
        searchInput.setPadding(dp(12), 0, dp(12), 0);
        searchInput.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams searchParams = matchWrap();
        searchParams.setMargins(0, dp(12), 0, dp(10));
        root.addView(searchInput, searchParams);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                loadStudents(currentQuery);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ScrollView scroll = new ScrollView(this);
        studentsContainer = new LinearLayout(this);
        studentsContainer.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(studentsContainer, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView footer = new TextView(this);
        footer.setText("البيانات محفوظة محليًا على هذا الجهاز");
        footer.setTextSize(12);
        footer.setTextColor(0xFF7A8798);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(10), 0, 0);
        root.addView(footer, matchWrap());

        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1565C0));
        return button;
    }

    private void loadStudents(String query) {
        databaseExecutor.execute(() -> {
            List<Student> students = query.isEmpty()
                    ? database.studentDao().getAll()
                    : database.studentDao().search(query);
            int count = database.studentDao().count();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                currentStudents = students;
                countText.setText("إجمالي الطلاب: " + count);
                renderStudents(students);
            });
        });
    }

    private void renderStudents(List<Student> students) {
        studentsContainer.removeAllViews();
        if (students.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(currentQuery.isEmpty()
                    ? "لا يوجد طلاب بعد.\nاضغط «إضافة طالب» للبدء."
                    : "لا توجد نتائج مطابقة للبحث.");
            empty.setTextColor(0xFF65758B);
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(12), dp(35), dp(12), dp(35));
            studentsContainer.addView(empty, matchWrap());
            return;
        }

        for (Student student : students) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(14), dp(12), dp(14), dp(10));
            LinearLayout.LayoutParams cardParams = matchWrap();
            cardParams.setMargins(0, 0, 0, dp(10));
            studentsContainer.addView(card, cardParams);

            TextView name = new TextView(this);
            name.setText(student.fullName);
            name.setTextSize(18);
            name.setTextColor(0xFF15345B);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(name, matchWrap());

            TextView details = new TextView(this);
            String info = "رقم الطالب: " + valueOrDash(student.studentNumber)
                    + "\nالصف/الشعبة: " + valueOrDash(student.className)
                    + "\nهاتف الطالب: " + valueOrDash(student.phone)
                    + "\nولي الأمر: " + valueOrDash(student.guardianName);
            if (student.notes != null && !student.notes.trim().isEmpty()) {
                info += "\nملاحظات: " + student.notes;
            }
            details.setText(info);
            details.setTextSize(14);
            details.setTextColor(0xFF53657A);
            details.setPadding(0, dp(6), 0, dp(8));
            card.addView(details, matchWrap());

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.LEFT);
            Button edit = makeButton("تعديل");
            edit.setOnClickListener(v -> showStudentDialog(student));
            actions.addView(edit, wrapWrap());

            Button delete = makeButton("حذف");
            delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFC62828));
            LinearLayout.LayoutParams deleteParams = wrapWrap();
            deleteParams.setMargins(dp(8), 0, 0, 0);
            delete.setOnClickListener(v -> confirmDelete(student));
            actions.addView(delete, deleteParams);
            card.addView(actions, matchWrap());
        }
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "—" : value;
    }

    private EditText createField(String hint, String initialValue) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setTextSize(15);
        field.setHint(hint);
        field.setText(initialValue == null ? "" : initialValue);
        field.setPadding(dp(12), 0, dp(12), 0);
        return field;
    }

    private void showStudentDialog(Student existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(4), dp(20), 0);
        form.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        EditText name = createField("الاسم الكامل *", existing == null ? "" : existing.fullName);
        EditText number = createField("رقم الطالب", existing == null ? "" : existing.studentNumber);
        EditText className = createField("الصف أو الشعبة", existing == null ? "" : existing.className);
        EditText phone = createField("رقم هاتف الطالب", existing == null ? "" : existing.phone);
        EditText guardian = createField("اسم ولي الأمر", existing == null ? "" : existing.guardianName);
        EditText notes = createField("ملاحظات", existing == null ? "" : existing.notes);

        addFormField(form, name);
        addFormField(form, number);
        addFormField(form, className);
        addFormField(form, phone);
        addFormField(form, guardian);
        addFormField(form, notes);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "إضافة طالب جديد" : "تعديل بيانات الطالب")
                .setView(scroll)
                .setNegativeButton("إلغاء", null)
                .setPositiveButton(existing == null ? "حفظ" : "تحديث", null)
                .create();

        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String fullName = name.getText().toString().trim();
            if (fullName.isEmpty()) {
                name.setError("الاسم الكامل مطلوب");
                name.requestFocus();
                return;
            }

            Student student = new Student(
                    fullName,
                    number.getText().toString().trim(),
                    className.getText().toString().trim(),
                    phone.getText().toString().trim(),
                    guardian.getText().toString().trim(),
                    notes.getText().toString().trim()
            );

            databaseExecutor.execute(() -> {
                if (existing == null) {
                    database.studentDao().insert(student);
                } else {
                    student.id = existing.id;
                    database.studentDao().update(student);
                }
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    dialog.dismiss();
                    Toast.makeText(this, existing == null ? "تم حفظ الطالب" : "تم تحديث البيانات",
                            Toast.LENGTH_SHORT).show();
                    loadStudents(currentQuery);
                });
            });
        }));
        dialog.show();
    }

    private void addFormField(LinearLayout form, EditText field) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 0, 0, dp(8));
        form.addView(field, params);
    }

    private void confirmDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("حذف الطالب")
                .setMessage("هل تريد حذف بيانات " + student.fullName + "؟ لا يمكن التراجع عن هذا الإجراء.")
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("حذف", (dialog, which) -> databaseExecutor.execute(() -> {
                    database.studentDao().delete(student);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "تم حذف الطالب", Toast.LENGTH_SHORT).show();
                        loadStudents(currentQuery);
                    });
                }))
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
