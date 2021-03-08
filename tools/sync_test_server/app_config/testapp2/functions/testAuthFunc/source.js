exports = ({mail, id}) => {
    // Auth function will fail for emails with a domain different to @androidtest.realm.io
    // or with id lower than 666
    if (!new RegExp("@androidtest.realm.io$").test(mail) || id < 666) {
        return 0;
    } else {
        // Use the users email as UID
        return mail;
    }
}
