use std::thread;
use std::time::Duration;

mod time;

fn start_heartbeat_interrupt() {
    thread::spawn(|| {
        loop {
            println!("hi");
            shuteye::sleep(Duration::from_nanos(3000));
        }
    });
}
