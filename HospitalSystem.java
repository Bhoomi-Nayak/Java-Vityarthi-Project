import java.util.*;
import java.io.*;

// ======================= MODEL CLASSES ==========================

// Patient Class
class Patient {
    private int id;
    private String name;
    private int age;
    private String phone;

    public Patient(int id, String name, int age, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.phone = phone;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPhone() { return phone; }

    public String toString() {
        return id + "," + name + "," + age + "," + phone;
    }

    public static Patient fromCsv(String line) {
        String[] p = line.split(",");
        return new Patient(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]), p[3]);
    }
}

// Doctor Class
class Doctor {
    private int id;
    private String name;
    private String specialization;

    public Doctor(int id, String name, String specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }

    public String toString() {
        return id + "," + name + "," + specialization;
    }

    public static Doctor fromCsv(String line) {
        String[] p = line.split(",");
        return new Doctor(Integer.parseInt(p[0]), p[1], p[2]);
    }
}

// Appointment Class
class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private String date;
    private String time;
    private String status;

    public Appointment(int id, int patientId, int doctorId, String date, String time, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() { return id; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toString() {
        return id + "," + patientId + "," + doctorId + "," + date + "," + time + "," + status;
    }

    public static Appointment fromCsv(String line) {
        String[] p = line.split(",");
        return new Appointment(Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                Integer.parseInt(p[2]), p[3], p[4], p[5]);
    }
}

// ======================= STORAGE MANAGER ==========================

class StorageManager {
    private static final String PATIENT_FILE = "patients.txt";
    private static final String DOCTOR_FILE = "doctors.txt";
    private static final String APPT_FILE = "appointments.txt";

    public static void init() {
        try {
            new File(PATIENT_FILE).createNewFile();
            new File(DOCTOR_FILE).createNewFile();
            new File(APPT_FILE).createNewFile();
        } catch (Exception e) {}
    }

    public static List<Patient> loadPatients() {
        List<Patient> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PATIENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty())
                list.add(Patient.fromCsv(line));
        } catch (Exception e) {}
        return list;
    }

    public static List<Doctor> loadDoctors() {
        List<Doctor> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DOCTOR_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty())
                list.add(Doctor.fromCsv(line));
        } catch (Exception e) {}
        return list;
    }

    public static List<Appointment> loadAppointments() {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(APPT_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty())
                list.add(Appointment.fromCsv(line));
        } catch (Exception e) {}
        return list;
    }

    public static <T> void saveList(String filename, List<T> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (T t : list) bw.write(t.toString() + "\n");
        } catch (Exception e) {}
    }

    public static void savePatients(List<Patient> list) {
        saveList(PATIENT_FILE, list);
    }

    public static void saveDoctors(List<Doctor> list) {
        saveList(DOCTOR_FILE, list);
    }

    public static void saveAppointments(List<Appointment> list) {
        saveList(APPT_FILE, list);
    }
}

// ======================= APPOINTMENT MANAGER ==========================

class AppointmentManager {
    private List<Patient> patients;
    private List<Doctor> doctors;
    private List<Appointment> appointments;
    private int nextPid = 1, nextDid = 1, nextAid = 1;

    public AppointmentManager() {
        StorageManager.init();

        patients = StorageManager.loadPatients();
        doctors = StorageManager.loadDoctors();
        appointments = StorageManager.loadAppointments();

        for (Patient p : patients) nextPid = Math.max(nextPid, p.getId()+1);
        for (Doctor d : doctors) nextDid = Math.max(nextDid, d.getId()+1);
        for (Appointment a : appointments) nextAid = Math.max(nextAid, a.getId()+1);

        if (doctors.isEmpty()) {
            addDoctor("Dr. Smith", "General");
            addDoctor("Dr. Riya", "Cardiology");
            addDoctor("Dr. Patel", "Dermatology");
            StorageManager.saveDoctors(doctors);
        }
    }

    public Patient addPatient(String name, int age, String phone) {
        Patient p = new Patient(nextPid++, name, age, phone);
        patients.add(p);
        StorageManager.savePatients(patients);
        return p;
    }

    public Doctor addDoctor(String name, String specialization) {
        Doctor d = new Doctor(nextDid++, name, specialization);
        doctors.add(d);
        StorageManager.saveDoctors(doctors);
        return d;
    }

    public List<Doctor> listDoctors() { return doctors; }

    public Appointment book(int pid, int did, String date, String time) {
        // Check conflict
        for (Appointment a : appointments) {
            if (a.getDoctorId() == did && a.getDate().equals(date) &&
                a.getTime().equals(time) && a.getStatus().equals("BOOKED"))
                return null;
        }
        Appointment ap = new Appointment(nextAid++, pid, did, date, time, "BOOKED");
        appointments.add(ap);
        StorageManager.saveAppointments(appointments);
        return ap;
    }

    public boolean cancel(int apptId) {
        for (Appointment a : appointments) {
            if (a.getId() == apptId && a.getStatus().equals("BOOKED")) {
                a.setStatus("CANCELLED");
                StorageManager.saveAppointments(appointments);
                return true;
            }
        }
        return false;
    }

    public boolean reschedule(int id, String newDate, String newTime) {
        Appointment target = null;

        for (Appointment a : appointments)
            if (a.getId() == id) target = a;

        if (target == null || !target.getStatus().equals("BOOKED"))
            return false;

        for (Appointment a : appointments) {
            if (a.getDoctorId()==target.getDoctorId() &&
                a.getDate().equals(newDate) &&
                a.getTime().equals(newTime) &&
                a.getStatus().equals("BOOKED"))
                return false;
        }

        Appointment newA = new Appointment(
            target.getId(), target.getPatientId(), target.getDoctorId(),
            newDate, newTime, "BOOKED"
        );

        for (int i=0;i<appointments.size();i++) {
            if (appointments.get(i).getId() == id) {
                appointments.set(i, newA);
                StorageManager.saveAppointments(appointments);
                return true;
            }
        }
        return false;
    }

    public List<Appointment> doctorSchedule(int did, String date) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : appointments)
            if (a.getDoctorId()==did && a.getDate().equals(date) && a.getStatus().equals("BOOKED"))
                out.add(a);
        return out;
    }
}

// ======================= MAIN PROGRAM ==========================

public class HospitalSystem {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        AppointmentManager mgr = new AppointmentManager();

        while (true) {
            System.out.println("\n===== HOSPITAL APPOINTMENT SYSTEM =====");
            System.out.println("1. Register Patient");
            System.out.println("2. List Doctors");
            System.out.println("3. Book Appointment");
            System.out.println("4. Cancel Appointment");
            System.out.println("5. Reschedule Appointment");
            System.out.println("6. Doctor's Daily Report");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            String c = sc.nextLine();

            switch (c) {
                case "1": registerPatient(mgr); break;
                case "2": listDoctors(mgr); break;
                case "3": book(mgr); break;
                case "4": cancel(mgr); break;
                case "5": reschedule(mgr); break;
                case "6": report(mgr); break;
                case "0": System.out.println("Thank you!"); return;
                default: System.out.println("Invalid choice!");
            }
        }
    }

    static void registerPatient(AppointmentManager mgr) {
        System.out.print("Enter name: ");
        String n = sc.nextLine();
        System.out.print("Enter age: ");
        int a = Integer.parseInt(sc.nextLine());
        System.out.print("Enter phone: ");
        String p = sc.nextLine();

        Patient pt = mgr.addPatient(n, a, p);
        System.out.println("Patient Registered with ID: " + pt.getId());
    }

    static void listDoctors(AppointmentManager mgr) {
        System.out.println("\nAvailable Doctors:");
        for (Doctor d : mgr.listDoctors()) {
            System.out.println(d.getId() + ". " + d.getName() + " (" + d.getSpecialization() + ")");
        }
    }

    static void book(AppointmentManager mgr) {
        System.out.print("Enter Patient ID: ");
        int pid = Integer.parseInt(sc.nextLine());
        System.out.print("Enter Doctor ID: ");
        int did = Integer.parseInt(sc.nextLine());
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();
        System.out.print("Enter Time (HH:MM): ");
        String time = sc.nextLine();

        Appointment ap = mgr.book(pid, did, date, time);

        if (ap == null)
            System.out.println("Slot NOT available!");
        else
            System.out.println("Appointment Booked! ID: " + ap.getId());
    }

    static void cancel(AppointmentManager mgr) {
        System.out.print("Enter Appointment ID: ");
        int id = Integer.parseInt(sc.nextLine());

        if (mgr.cancel(id))
            System.out.println("Appointment Cancelled.");
        else
            System.out.println("Invalid Appointment.");
    }

    static void reschedule(AppointmentManager mgr) {
        System.out.print("Enter Appointment ID: ");
        int id = Integer.parseInt(sc.nextLine());
        System.out.print("Enter New Date (YYYY-MM-DD): ");
        String newDate = sc.nextLine();
        System.out.print("Enter New Time (HH:MM): ");
        String newTime = sc.nextLine();

        if (mgr.reschedule(id, newDate, newTime))
            System.out.println("Appointment Rescheduled.");
        else
            System.out.println("Reschedule Failed.");
    }

    static void report(AppointmentManager mgr) {
        System.out.print("Enter Doctor ID: ");
        int did = Integer.parseInt(sc.nextLine());
        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = sc.nextLine();

        List<Appointment> list = mgr.doctorSchedule(did, date);

        System.out.println("\nSchedule for Doctor ID " + did + " on " + date + ":");
        if (list.isEmpty())
            System.out.println("No Appointments.");
        else
            for (Appointment a : list)
                System.out.println("Appt ID: " + a.getId() + ", Patient ID: " + a.getPatientId() + ", Time: " + a.getTime());
    }
}
