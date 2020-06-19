exports = ({mail, id}) => {
    if (mail != "myfakemail@mongodb.com" || id != 666) {
        return 0;
    } else {
        return "works";
    }
}
