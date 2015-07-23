module ui/derivetemplate

derivetemplate testd XYZ {
  page XYZ(){

  }
}

derive testd testpage

page testcall(){
  navigate testpage(){""}
}

