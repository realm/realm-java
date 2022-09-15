exports = ({mail, id}) => {
    // Auth function will fail for emails with a domain different to @10gen.com
    // or with id lower than 666
    if (!new RegExp("@10gen.com$").test(mail) || id < 666) {
        return 0;
    } else {
        // Use the users email as UID
        return mail;
    }
}
