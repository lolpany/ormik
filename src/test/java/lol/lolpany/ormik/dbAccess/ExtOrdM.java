package lol.lolpany.ormik.dbAccess;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "ord_m")
public class ExtOrdM implements lol.lolpany.ormik.reinsertableBeans.ILimitedBean {
    @Column(name = "b_type")
    public int type;
    @Id
    @Column(name = "b_regnum")
    public Long regnum;
    @Column(name = "b_sdate")
    public Date sdate;
    @Column(name = "b_stime")
    public String stime;
    @Column(name = "b_tdate")
    public Date tdate;
    @Column(name = "b_ttime")
    public String ttime;
    @Column(name = "b_suser")
    public int suser;
    @Column(name = "b_profc")
    public int profc;
    @Column(name = "b_isserv")
    public int isserv;
    @Column(name = "b_curat")
    public int curat;
    @Column(name = "b_respons")
    public int respons;
    @Column(name = "b_n")
    public int n;
    @Column(name = "b_msyst")
    public int msyst;
    @Column(name = "b_med")
    public Integer med;
    @Column(name = "b_gds")
    public Integer gds;
    @Column(name = "b_source")
    public int source;
    @Column(name = "b_uk")
    public int uk;
    @Column(name = "b_summit")
    public int summit;
    @Column(name = "b_agent")
    public long agent;
    @Column(name = "b_isusers")
    public Integer isusers;
    @Column(name = "b_buytyp")
    public Integer buytyp;
    @Column(name = "b_invto")
    public int invto;
    @Column(name = "b_paysyst")
    public int paysyst;
    @Column(name = "b_admit")
    public int admit;
    @Column(name = "b_state")
    public Integer state;
    @Column(name = "b_town")
    public Integer town;
    @Column(name = "b_buy")
    public long buy;
    @Column(name = "b_partner")
    public int partner;
    @Column(name = "b_tpcontr")
    public int tpcontr;
    @Column(name = "b_brefn")
    public String brefn;
    @Column(name = "b_refname")
    public String refname;
    @Column(name = "b_idsend")
    public long idsend;
    @Column(name = "b_sumr")
    public int sumr;
    @Column(name = "b_invf")
    public int invf;
    @Column(name = "b_author")
    public int author;
    @Column(name = "b_bankon")
    public int bankon;
    @Column(name = "b_bank")
    public long bank;
    @Column(name = "b_showm")
    public int showm;
    @Column(name = "b_showins")
    public int showins;
    @Column(name = "b_showout")
    public int showout;
    @Column(name = "b_showvat")
    public int showvat;
    @Column(name = "b_targ")
    public int targ;
    @Column(name = "b_id_gr")
    public int idGr;
    @Column(name = "b_cou_gr")
    public int couGr;
    @Column(name = "b_lead")
    public String lead;
    @Column(name = "b_ordhzt")
    public int ordhzt;
    @Column(name = "b_ishzt")
    public int ishzt;
    @Column(name = "b_showcdr")
    public int showcdr;
    @Column(name = "b_showvdr")
    public int showvdr;
    @Column(name = "b_showccar")
    public int showccar;
    @Column(name = "b_showvcar")
    public int showvcar;
    @Column(name = "b_showcanc")
    public int showcanc;
    @Column(name = "b_tz")
    public int tz;
    @Column(name = "b_csign")
    public int csign;
    @Column(name = "b_fsign")
    public int fsign;
    @Column(name = "b_asign")
    public int asign;
    @Column(name = "b_esign")
    public int esign;
    @Column(name = "b_ds_sign")
    public int dsSign;
    @Column(name = "b_psign")
    public int psign;
    @Column(name = "b_nsign")
    public int nsign;
    @Column(name = "b_ksign")
    public int ksign;
    @Column(name = "b_is_acc")
    public int isAcc;
    @Column(name = "b_is_ext")
    public int isExt;
    @Column(name = "b_is_ds")
    public int isDs;
    @Column(name = "b_is_pas")
    public int isPas;
    @Column(name = "b_is_com")
    public int isCom;
    @Column(name = "b_isplpay")
    public int isplpay;
    @Column(name = "b_dbeg")
    public Date dbeg;
    @Column(name = "b_dend")
    public Date dend;
    @Column(name = "b_dcont")
    public Date dcont;
    @Column(name = "b_ccont")
    public Date ccont;
    @Column(name = "b_stat")
    public int stat;
    @Column(name = "b_dstat")
    public Date dstat;
    @Column(name = "b_as_acc")
    public int asAcc;
    @Column(name = "b_es_acc")
    public int esAcc;
    @Column(name = "b_os_acc")
    public int osAcc;
    @Column(name = "b_infin")
    public int infin;
    @Column(name = "b_indif")
    public int indif;
    @Column(name = "b_outdif")
    public int outdif;
    @Column(name = "b_flp")
    public int flp;
    @Column(name = "b_flp1")
    public int flp1;
    @Column(name = "b_flp2")
    public int flp2;
    @Column(name = "b_fsuppl")
    public int fsuppl;
    @Column(name = "b_paid")
    public int paid;
    @Column(name = "b_dpaid")
    public Date dpaid;
    @Column(name = "b_contpay")
    public int contpay;
    @Column(name = "b_datepay")
    public Date datepay;
    @Column(name = "b_fstat")
    public int fstat;
    @Column(name = "b_fsdate")
    public Date fsdate;
    @Column(name = "b_inv")
    public int inv;
    @Column(name = "b_invst")
    public int invst;
    @Column(name = "b_dinvst")
    public Date dinvst;
    @Column(name = "b_istat")
    public int istat;
    @Column(name = "b_confnum")
    public int confnum;
    @Column(name = "b_confst")
    public int confst;
    @Column(name = "b_cstat")
    public int cstat;
    @Column(name = "b_change")
    public int change;
    @Column(name = "b_uscng")
    public int uscng;
    @Column(name = "b_curr1")
    public int curr1;
    @Column(name = "b_cmarj")
    public BigDecimal cmarj;
    @Column(name = "b_occost1")
    public BigDecimal occost1;
    @Column(name = "b_occost3")
    public BigDecimal occost3;
    @Column(name = "b_oscost3")
    public BigDecimal oscost3;
    @Column(name = "b_ocsum3")
    public BigDecimal ocsum3;
    @Column(name = "b_accost1")
    public BigDecimal accost1;
    @Column(name = "b_accost3")
    public BigDecimal accost3;
    @Column(name = "b_ascost3")
    public BigDecimal ascost3;
    @Column(name = "b_acsum3")
    public BigDecimal acsum3;
    @Column(name = "b_eccost1")
    public BigDecimal eccost1;
    @Column(name = "b_eccost3")
    public BigDecimal eccost3;
    @Column(name = "b_escost3")
    public BigDecimal escost3;
    @Column(name = "b_ecsum3")
    public BigDecimal ecsum3;
    @Column(name = "b_dsccost1")
    public BigDecimal dsccost1;
    @Column(name = "b_dsccost3")
    public BigDecimal dsccost3;
    @Column(name = "b_dsscost3")
    public BigDecimal dsscost3;
    @Column(name = "b_dscsum3")
    public BigDecimal dscsum3;
    @Column(name = "b_pccost1")
    public BigDecimal pccost1;
    @Column(name = "b_pccost3")
    public BigDecimal pccost3;
    @Column(name = "b_pscost3")
    public BigDecimal pscost3;
    @Column(name = "b_pcsum3")
    public BigDecimal pcsum3;
    @Column(name = "b_tacsum1")
    public BigDecimal tacsum1;
    @Column(name = "b_tacsum3")
    public BigDecimal tacsum3;
    @Column(name = "b_islock1")
    public BigDecimal islock1;
    @Column(name = "b_islock3")
    public BigDecimal islock3;
    @Column(name = "b_locked1")
    public BigDecimal locked1;
    @Column(name = "b_locked3")
    public BigDecimal locked3;
    @Column(name = "b_ofcost1")
    public BigDecimal ofcost1;
    @Column(name = "b_ofcost3")
    public BigDecimal ofcost3;
    @Column(name = "b_sfcost3")
    public BigDecimal sfcost3;
    @Column(name = "b_ofsum3")
    public BigDecimal ofsum3;
    @Column(name = "b_afcost1")
    public BigDecimal afcost1;
    @Column(name = "b_afcost3")
    public BigDecimal afcost3;
    @Column(name = "b_afscst3")
    public BigDecimal afscst3;
    @Column(name = "b_afsum3")
    public BigDecimal afsum3;
    @Column(name = "b_efcost1")
    public BigDecimal efcost1;
    @Column(name = "b_efcost3")
    public BigDecimal efcost3;
    @Column(name = "b_efscst3")
    public BigDecimal efscst3;
    @Column(name = "b_efsum3")
    public BigDecimal efsum3;
    @Column(name = "b_dsfcost1")
    public BigDecimal dsfcost1;
    @Column(name = "b_dsfcost3")
    public BigDecimal dsfcost3;
    @Column(name = "b_dsfscst3")
    public BigDecimal dsfscst3;
    @Column(name = "b_dsfsum3")
    public BigDecimal dsfsum3;
    @Column(name = "b_pfcost1")
    public BigDecimal pfcost1;
    @Column(name = "b_pfcost3")
    public BigDecimal pfcost3;
    @Column(name = "b_pfscst3")
    public BigDecimal pfscst3;
    @Column(name = "b_pfsum3")
    public BigDecimal pfsum3;
    @Column(name = "b_tafsum1")
    public BigDecimal tafsum1;
    @Column(name = "b_tafsum3")
    public BigDecimal tafsum3;
    @Column(name = "b_vpsum11")
    public BigDecimal vpsum11;
    @Column(name = "b_vpsum13")
    public BigDecimal vpsum13;
    @Column(name = "b_cpsum11")
    public BigDecimal cpsum11;
    @Column(name = "b_cpsum13")
    public BigDecimal cpsum13;
    @Column(name = "b_vpsum23")
    public BigDecimal vpsum23;
    @Column(name = "b_cpsum23")
    public BigDecimal cpsum23;
    @Column(name = "b_ssum1")
    public BigDecimal ssum1;
    @Column(name = "b_ssum3")
    public BigDecimal ssum3;
    @Column(name = "b_scom1")
    public BigDecimal scom1;
    @Column(name = "b_scom3")
    public BigDecimal scom3;
    @Column(name = "b_fsum1")
    public BigDecimal fsum1;
    @Column(name = "b_fsum3")
    public BigDecimal fsum3;
    @Column(name = "b_psum1")
    public BigDecimal psum1;
    @Column(name = "b_psum3")
    public BigDecimal psum3;
    @Column(name = "b_rsum1")
    public BigDecimal rsum1;
    @Column(name = "b_rsum3")
    public BigDecimal rsum3;
    @Column(name = "b_bsum1")
    public BigDecimal bsum1;
    @Column(name = "b_bsum3")
    public BigDecimal bsum3;
    @Column(name = "b_tsum1")
    public BigDecimal tsum1;
    @Column(name = "b_tsum3")
    public BigDecimal tsum3;
    @Column(name = "b_max1c")
    public Date max1c;
    @Column(name = "b_isnull")
    public int isnull;
    @Column(name = "b_markup")
    public int markup;
    @Column(name = "b_markpct")
    public BigDecimal markpct;
    @Column(name = "b_user")
    public int user;
    @Column(name = "b_usdate")
    public Date usdate;
    @Column(name = "b_sysdate")
    public Date sysdate;
    @Column(name = "b_time")
    public String time;
    @Column(name = "b_paysubs")
    public int paysubs;
    @Column(name = "b_bnumdat")
    public int bnumdat;
    @Column(name = "b_grantbuy")
    public int grantbuy;
    @Column(name = "b_showsmt")
    public int showsmt;
    @Column(name = "b_showroom")
    public int showroom;
    @Column(name = "b_wg")
    public int wg;
    @Column(name = "b_dcstat")
    public Date dcstat;
    @Column(name = "b_tcstat")
    public String tcstat;
    @Column(name = "b_distat")
    public Date distat;
    @Column(name = "b_tistat")
    public String tistat;
    @Column(name = "b_monocurr")
    public int monocurr;
    @Column(name = "b_oscost1")
    public BigDecimal oscost1;
    @Column(name = "b_ocsum1")
    public BigDecimal ocsum1;
    @Column(name = "b_sfcost1")
    public BigDecimal sfcost1;
    @Column(name = "b_ofsum1")
    public BigDecimal ofsum1;
    @Column(name = "b_cpsum21")
    public BigDecimal cpsum21;
    @Column(name = "b_showrt")
    public int showrt;
    @Column(name = "b_base")
    public String base;
    @Column(name = "b_fcond")
    public int fcond;
    @Column(name = "b_gain1")
    public BigDecimal gain1;
    @Column(name = "b_gain3")
    public BigDecimal gain3;
    @Column(name = "b_isgain")
    public int isgain;

    public void setRegnum(long regnum) {
        this.regnum = regnum;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSdate(Date sdate) {
        this.sdate = sdate;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public void setTdate(Date tdate) {
        this.tdate = tdate;
    }

    public void setTtime(String ttime) {
        this.ttime = ttime;
    }

    public void setSuser(int suser) {
        this.suser = suser;
    }

    public void setProfc(int profc) {
        this.profc = profc;
    }

    public void setIsserv(int isserv) {
        this.isserv = isserv;
    }

    public void setCurat(int curat) {
        this.curat = curat;
    }

    public void setRespons(int respons) {
        this.respons = respons;
    }

    public void setN(int n) {
        this.n = n;
    }

    public void setMsyst(int msyst) {
        this.msyst = msyst;
    }

    public void setMed(Integer med) {
        this.med = med;
    }

    public void setGds(Integer gds) {
        this.gds = gds;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setUk(int uk) {
        this.uk = uk;
    }

    public void setSummit(int summit) {
        this.summit = summit;
    }

    public void setAgent(long agent) {
        this.agent = agent;
    }

    public void setIsusers(Integer isusers) {
        this.isusers = isusers;
    }

    public void setBuytyp(Integer buytyp) {
        this.buytyp = buytyp;
    }

    public void setInvto(int invto) {
        this.invto = invto;
    }

    public void setPaysyst(int paysyst) {
        this.paysyst = paysyst;
    }

    public void setAdmit(int admit) {
        this.admit = admit;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setTown(Integer town) {
        this.town = town;
    }

    public void setBuy(long buy) {
        this.buy = buy;
    }

    public void setPartner(int partner) {
        this.partner = partner;
    }

    public void setTpcontr(int tpcontr) {
        this.tpcontr = tpcontr;
    }

    public void setBrefn(String brefn) {
        this.brefn = brefn;
    }

    public void setRefname(String refname) {
        this.refname = refname;
    }

    public void setIdsend(long idsend) {
        this.idsend = idsend;
    }

    public void setSumr(int sumr) {
        this.sumr = sumr;
    }

    public void setInvf(int invf) {
        this.invf = invf;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public void setBankon(int bankon) {
        this.bankon = bankon;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public void setShowm(int showm) {
        this.showm = showm;
    }

    public void setShowins(int showins) {
        this.showins = showins;
    }

    public void setShowout(int showout) {
        this.showout = showout;
    }

    public void setShowvat(int showvat) {
        this.showvat = showvat;
    }

    public void setTarg(int targ) {
        this.targ = targ;
    }

    public void setIdGr(int idGr) {
        this.idGr = idGr;
    }

    public void setCouGr(int couGr) {
        this.couGr = couGr;
    }

    public void setLead(String lead) {
        this.lead = lead;
    }

    public void setOrdhzt(int ordhzt) {
        this.ordhzt = ordhzt;
    }

    public void setIshzt(int ishzt) {
        this.ishzt = ishzt;
    }

    public void setShowcdr(int showcdr) {
        this.showcdr = showcdr;
    }

    public void setShowvdr(int showvdr) {
        this.showvdr = showvdr;
    }

    public void setShowccar(int showccar) {
        this.showccar = showccar;
    }

    public void setShowvcar(int showvcar) {
        this.showvcar = showvcar;
    }

    public void setShowcanc(int showcanc) {
        this.showcanc = showcanc;
    }

    public void setTz(int tz) {
        this.tz = tz;
    }

    public void setCsign(int csign) {
        this.csign = csign;
    }

    public void setFsign(int fsign) {
        this.fsign = fsign;
    }

    public void setAsign(int asign) {
        this.asign = asign;
    }

    public void setEsign(int esign) {
        this.esign = esign;
    }

    public void setDsSign(int dsSign) {
        this.dsSign = dsSign;
    }

    public void setPsign(int psign) {
        this.psign = psign;
    }

    public void setNsign(int nsign) {
        this.nsign = nsign;
    }

    public void setKsign(int ksign) {
        this.ksign = ksign;
    }

    public void setIsAcc(int isAcc) {
        this.isAcc = isAcc;
    }

    public void setIsExt(int isExt) {
        this.isExt = isExt;
    }

    public void setIsDs(int isDs) {
        this.isDs = isDs;
    }

    public void setIsPas(int isPas) {
        this.isPas = isPas;
    }

    public void setIsCom(int isCom) {
        this.isCom = isCom;
    }

    public void setIsplpay(int isplpay) {
        this.isplpay = isplpay;
    }

    public void setDbeg(Date dbeg) {
        this.dbeg = dbeg;
    }

    public void setDend(Date dend) {
        this.dend = dend;
    }

    public void setDcont(Date dcont) {
        this.dcont = dcont;
    }

    public void setCcont(Date ccont) {
        this.ccont = ccont;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public void setDstat(Date dstat) {
        this.dstat = dstat;
    }

    public void setAsAcc(int asAcc) {
        this.asAcc = asAcc;
    }

    public void setEsAcc(int esAcc) {
        this.esAcc = esAcc;
    }

    public void setOsAcc(int osAcc) {
        this.osAcc = osAcc;
    }

    public void setInfin(int infin) {
        this.infin = infin;
    }

    public void setIndif(int indif) {
        this.indif = indif;
    }

    public void setOutdif(int outdif) {
        this.outdif = outdif;
    }

    public void setFlp(int flp) {
        this.flp = flp;
    }

    public void setFlp1(int flp1) {
        this.flp1 = flp1;
    }

    public void setFlp2(int flp2) {
        this.flp2 = flp2;
    }

    public void setFsuppl(int fsuppl) {
        this.fsuppl = fsuppl;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public void setDpaid(Date dpaid) {
        this.dpaid = dpaid;
    }

    public void setContpay(int contpay) {
        this.contpay = contpay;
    }

    public void setDatepay(Date datepay) {
        this.datepay = datepay;
    }

    public void setFstat(int fstat) {
        this.fstat = fstat;
    }

    public void setFsdate(Date fsdate) {
        this.fsdate = fsdate;
    }

    public void setInv(int inv) {
        this.inv = inv;
    }

    public void setInvst(int invst) {
        this.invst = invst;
    }

    public void setDinvst(Date dinvst) {
        this.dinvst = dinvst;
    }

    public void setIstat(int istat) {
        this.istat = istat;
    }

    public void setConfnum(int confnum) {
        this.confnum = confnum;
    }

    public void setConfst(int confst) {
        this.confst = confst;
    }

    public void setCstat(int cstat) {
        this.cstat = cstat;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public void setUscng(int uscng) {
        this.uscng = uscng;
    }

    public void setCurr1(int curr1) {
        this.curr1 = curr1;
    }

    public void setCmarj(BigDecimal cmarj) {
        this.cmarj = cmarj;
    }

    public void setOccost1(BigDecimal occost1) {
        this.occost1 = occost1;
    }

    public void setOccost3(BigDecimal occost3) {
        this.occost3 = occost3;
    }

    public void setOscost3(BigDecimal oscost3) {
        this.oscost3 = oscost3;
    }

    public void setOcsum3(BigDecimal ocsum3) {
        this.ocsum3 = ocsum3;
    }

    public void setAccost1(BigDecimal accost1) {
        this.accost1 = accost1;
    }

    public void setAccost3(BigDecimal accost3) {
        this.accost3 = accost3;
    }

    public void setAscost3(BigDecimal ascost3) {
        this.ascost3 = ascost3;
    }

    public void setAcsum3(BigDecimal acsum3) {
        this.acsum3 = acsum3;
    }

    public void setEccost1(BigDecimal eccost1) {
        this.eccost1 = eccost1;
    }

    public void setEccost3(BigDecimal eccost3) {
        this.eccost3 = eccost3;
    }

    public void setEscost3(BigDecimal escost3) {
        this.escost3 = escost3;
    }

    public void setEcsum3(BigDecimal ecsum3) {
        this.ecsum3 = ecsum3;
    }

    public void setDsccost1(BigDecimal dsccost1) {
        this.dsccost1 = dsccost1;
    }

    public void setDsccost3(BigDecimal dsccost3) {
        this.dsccost3 = dsccost3;
    }

    public void setDsscost3(BigDecimal dsscost3) {
        this.dsscost3 = dsscost3;
    }

    public void setDscsum3(BigDecimal dscsum3) {
        this.dscsum3 = dscsum3;
    }

    public void setPccost1(BigDecimal pccost1) {
        this.pccost1 = pccost1;
    }

    public void setPccost3(BigDecimal pccost3) {
        this.pccost3 = pccost3;
    }

    public void setPscost3(BigDecimal pscost3) {
        this.pscost3 = pscost3;
    }

    public void setPcsum3(BigDecimal pcsum3) {
        this.pcsum3 = pcsum3;
    }

    public void setTacsum1(BigDecimal tacsum1) {
        this.tacsum1 = tacsum1;
    }

    public void setTacsum3(BigDecimal tacsum3) {
        this.tacsum3 = tacsum3;
    }

    public void setIslock1(BigDecimal islock1) {
        this.islock1 = islock1;
    }

    public void setIslock3(BigDecimal islock3) {
        this.islock3 = islock3;
    }

    public void setLocked1(BigDecimal locked1) {
        this.locked1 = locked1;
    }

    public void setLocked3(BigDecimal locked3) {
        this.locked3 = locked3;
    }

    public void setOfcost1(BigDecimal ofcost1) {
        this.ofcost1 = ofcost1;
    }

    public void setOfcost3(BigDecimal ofcost3) {
        this.ofcost3 = ofcost3;
    }

    public void setSfcost3(BigDecimal sfcost3) {
        this.sfcost3 = sfcost3;
    }

    public void setOfsum3(BigDecimal ofsum3) {
        this.ofsum3 = ofsum3;
    }

    public void setAfcost1(BigDecimal afcost1) {
        this.afcost1 = afcost1;
    }

    public void setAfcost3(BigDecimal afcost3) {
        this.afcost3 = afcost3;
    }

    public void setAfscst3(BigDecimal afscst3) {
        this.afscst3 = afscst3;
    }

    public void setAfsum3(BigDecimal afsum3) {
        this.afsum3 = afsum3;
    }

    public void setEfcost1(BigDecimal efcost1) {
        this.efcost1 = efcost1;
    }

    public void setEfcost3(BigDecimal efcost3) {
        this.efcost3 = efcost3;
    }

    public void setEfscst3(BigDecimal efscst3) {
        this.efscst3 = efscst3;
    }

    public void setEfsum3(BigDecimal efsum3) {
        this.efsum3 = efsum3;
    }

    public void setDsfcost1(BigDecimal dsfcost1) {
        this.dsfcost1 = dsfcost1;
    }

    public void setDsfcost3(BigDecimal dsfcost3) {
        this.dsfcost3 = dsfcost3;
    }

    public void setDsfscst3(BigDecimal dsfscst3) {
        this.dsfscst3 = dsfscst3;
    }

    public void setDsfsum3(BigDecimal dsfsum3) {
        this.dsfsum3 = dsfsum3;
    }

    public void setPfcost1(BigDecimal pfcost1) {
        this.pfcost1 = pfcost1;
    }

    public void setPfcost3(BigDecimal pfcost3) {
        this.pfcost3 = pfcost3;
    }

    public void setPfscst3(BigDecimal pfscst3) {
        this.pfscst3 = pfscst3;
    }

    public void setPfsum3(BigDecimal pfsum3) {
        this.pfsum3 = pfsum3;
    }

    public void setTafsum1(BigDecimal tafsum1) {
        this.tafsum1 = tafsum1;
    }

    public void setTafsum3(BigDecimal tafsum3) {
        this.tafsum3 = tafsum3;
    }

    public void setVpsum11(BigDecimal vpsum11) {
        this.vpsum11 = vpsum11;
    }

    public void setVpsum13(BigDecimal vpsum13) {
        this.vpsum13 = vpsum13;
    }

    public void setCpsum11(BigDecimal cpsum11) {
        this.cpsum11 = cpsum11;
    }

    public void setCpsum13(BigDecimal cpsum13) {
        this.cpsum13 = cpsum13;
    }

    public void setVpsum23(BigDecimal vpsum23) {
        this.vpsum23 = vpsum23;
    }

    public void setCpsum23(BigDecimal cpsum23) {
        this.cpsum23 = cpsum23;
    }

    public void setSsum1(BigDecimal ssum1) {
        this.ssum1 = ssum1;
    }

    public void setSsum3(BigDecimal ssum3) {
        this.ssum3 = ssum3;
    }

    public void setScom1(BigDecimal scom1) {
        this.scom1 = scom1;
    }

    public void setScom3(BigDecimal scom3) {
        this.scom3 = scom3;
    }

    public void setFsum1(BigDecimal fsum1) {
        this.fsum1 = fsum1;
    }

    public void setFsum3(BigDecimal fsum3) {
        this.fsum3 = fsum3;
    }

    public void setPsum1(BigDecimal psum1) {
        this.psum1 = psum1;
    }

    public void setPsum3(BigDecimal psum3) {
        this.psum3 = psum3;
    }

    public void setRsum1(BigDecimal rsum1) {
        this.rsum1 = rsum1;
    }

    public void setRsum3(BigDecimal rsum3) {
        this.rsum3 = rsum3;
    }

    public void setBsum1(BigDecimal bsum1) {
        this.bsum1 = bsum1;
    }

    public void setBsum3(BigDecimal bsum3) {
        this.bsum3 = bsum3;
    }

    public void setTsum1(BigDecimal tsum1) {
        this.tsum1 = tsum1;
    }

    public void setTsum3(BigDecimal tsum3) {
        this.tsum3 = tsum3;
    }

    public void setMax1c(Date max1c) {
        this.max1c = max1c;
    }

    public void setIsnull(int isnull) {
        this.isnull = isnull;
    }

    public void setMarkup(int markup) {
        this.markup = markup;
    }

    public void setMarkpct(BigDecimal markpct) {
        this.markpct = markpct;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public void setUsdate(Date usdate) {
        this.usdate = usdate;
    }

    public void setSysdate(Date sysdate) {
        this.sysdate = sysdate;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPaysubs(int paysubs) {
        this.paysubs = paysubs;
    }

    public void setBnumdat(int bnumdat) {
        this.bnumdat = bnumdat;
    }

    public void setGrantbuy(int grantbuy) {
        this.grantbuy = grantbuy;
    }

    public void setShowsmt(int showsmt) {
        this.showsmt = showsmt;
    }

    public void setShowroom(int showroom) {
        this.showroom = showroom;
    }

    public void setWg(int wg) {
        this.wg = wg;
    }

    public void setDcstat(Date dcstat) {
        this.dcstat = dcstat;
    }

    public void setTcstat(String tcstat) {
        this.tcstat = tcstat;
    }

    public void setDistat(Date distat) {
        this.distat = distat;
    }

    public void setTistat(String tistat) {
        this.tistat = tistat;
    }

    public void setMonocurr(int monocurr) {
        this.monocurr = monocurr;
    }

    public void setOscost1(BigDecimal oscost1) {
        this.oscost1 = oscost1;
    }

    public void setOcsum1(BigDecimal ocsum1) {
        this.ocsum1 = ocsum1;
    }

    public void setSfcost1(BigDecimal sfcost1) {
        this.sfcost1 = sfcost1;
    }

    public void setOfsum1(BigDecimal ofsum1) {
        this.ofsum1 = ofsum1;
    }

    public void setCpsum21(BigDecimal cpsum21) {
        this.cpsum21 = cpsum21;
    }

    public void setShowrt(int showrt) {
        this.showrt = showrt;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setFcond(int fcond) {
        this.fcond = fcond;
    }

    public void setGain1(BigDecimal gain1) {
        this.gain1 = gain1;
    }

    public void setGain3(BigDecimal gain3) {
        this.gain3 = gain3;
    }

    public void setIsgain(int isgain) {
        this.isgain = isgain;
    }

}
